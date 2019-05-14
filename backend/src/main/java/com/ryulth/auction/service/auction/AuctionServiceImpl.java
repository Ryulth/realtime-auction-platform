package com.ryulth.auction.service.auction;

import com.ryulth.auction.domain.Auction;
import com.ryulth.auction.domain.Product;
import com.ryulth.auction.pojo.model.AuctionEvent;
import com.ryulth.auction.pojo.model.AuctionEventType;
import com.ryulth.auction.pojo.model.AuctionType;
import com.ryulth.auction.pojo.request.AuctionEnrollRequest;
import com.ryulth.auction.pojo.request.AuctionEventRequest;
import com.ryulth.auction.pojo.response.AuctionDataResponse;
import com.ryulth.auction.pojo.response.AuctionEventsResponse;
import com.ryulth.auction.pojo.response.AuctionListResponse;
import com.ryulth.auction.repository.AuctionRepository;
import com.ryulth.auction.repository.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.RedisSystemException;
import org.springframework.data.redis.connection.stream.ObjectRecord;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.connection.stream.StreamRecords;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StreamOperations;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Primary
public class AuctionServiceImpl implements AuctionService {
    private static Logger logger = LoggerFactory.getLogger(AuctionServiceImpl.class);
    @Autowired
    AuctionRepository auctionRepository;
    @Autowired
    ProductRepository productRepository;
    @Autowired
    RedisTemplate redisTemplate;
    @Autowired
    AuctionEventService auctionEventService;

    private static final String AUCTION_TYPE_REDIS = "ryulth:auction:type:";
    private static final String AUCTION_EVENTS_REDIS = "ryulth:auction:events:";
    private static final String AUCTION_ONGOING_REDIS = "ryulth:auction:ongoing:";

    @Override
    public Long enrollAuction(AuctionEnrollRequest auctionEnrollRequest) {
        AuctionType auctionType = auctionEnrollRequest.getAuctionTypeEnum();
        if (auctionType == AuctionType.ERROR) {
            return -1L;
        }
        ValueOperations vop = redisTemplate.opsForValue();
        long productId = auctionEnrollRequest.getProductId();

        Product product = productRepository.getOne(productId);
        product.setOnSale(1);
        productRepository.save(product);

        Auction auction = auctionRepository.findByProductId(productId)
                .orElse(Auction.builder()
                        .productId(productId)
                        .auctionType(auctionType.getValue())
                        .startTime(product.getStartTime())
                        .endTime(product.getEndTime())
                        .price(product.getLowerLimit())
                        .version(0L)
                        .build());
        auctionRepository.save(auction);

        long auctionId = auction.getId();
        AuctionEvent auctionEvent = AuctionEvent.builder()
                .auctionEventType(AuctionEventType.ENROLL)
                .version(0L)
                .price(product.getLowerLimit())
                .build();

        try {
            xAdd(AUCTION_EVENTS_REDIS + auctionId, auctionId + "-0", auctionEvent);
            vop.set(AUCTION_ONGOING_REDIS + auctionId, true);
        } catch (RedisSystemException e) {
            logger.info("이미 등록되있음");
        }

        vop.set(AUCTION_TYPE_REDIS + auctionId, auctionType.getValue());

        return auctionId;
    }

    @Override
    public AuctionListResponse getAllAuctions() {
        List<Auction> auctions = auctionRepository.findAll();
        return AuctionListResponse.builder().auctions(auctions).build();
    }

    @Override
    public AuctionDataResponse getAuction(Long auctionId) {
        Auction auction = auctionRepository.findById(auctionId).orElse(null);
        Product product = productRepository.findById(auction.getProductId()).orElse(null);
        return AuctionDataResponse.builder()
                .auctionEvents(getAuctionEvents(auctionId).getAuctionEvents())
                .auction(auction)
                .product(product)
                .build();
    }

    @Override
    public AuctionEventsResponse getAuctionEvents(Long auctionId) {
        StreamOperations sop = redisTemplate.opsForStream();
        List<ObjectRecord<String, AuctionEvent>> objectRecords = sop
                .read(AuctionEvent.class, StreamOffset.fromStart(AUCTION_EVENTS_REDIS + auctionId));
        List<AuctionEvent> auctionEvents = objectRecords.stream()
                .map(o -> o.getValue())
                .collect(Collectors.toList());
        return AuctionEventsResponse.builder()
                .auctionEvents(auctionEvents)
                .build();
    }

    @Override
    public AuctionEventsResponse eventAuction(Long auctionId, AuctionEventRequest auctionEventRequest) throws IOException {
        ValueOperations vop = redisTemplate.opsForValue();
        AuctionType auctionType = AuctionType.fromText((String) vop.get(AUCTION_TYPE_REDIS + auctionId));
        switch (auctionType) {
            case BASIC:
                return auctionEventService.basicAuctionEvent(auctionId, auctionEventRequest);
            case LIVE:
                return auctionEventService.liveAuctionEvent(auctionId, auctionEventRequest);
            case ERROR:
            default:
                return null;
        }
    }

    @Override
    public void endAuction() {
        scheduledEndAuction();
    }

    @Scheduled(initialDelay = 5000, fixedDelay = 1000)
    @Transactional
    protected void scheduledEndAuction() {
        ZonedDateTime now = ZonedDateTime.now(ZoneId.of("Asia/Seoul"));
        List<Auction> auctions = auctionRepository.findByEndTimeLessThanEqualAndOnAuction(now, 1);
        auctions.stream().forEach(a -> {
            a.setOnAuction(0);
            auctionRepository.save(a);
        });
        //System.out.println(auctions);
    }

    private <T> void xAdd(String key, String versionId, T data) throws RedisSystemException {
        StreamOperations sop = redisTemplate.opsForStream();
        ObjectRecord<String, T> record = StreamRecords.newRecord()
                .in(key)
                .withId(versionId)
                .ofObject(data);
        sop.add(record);
    }
}
