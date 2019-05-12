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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.RedisSystemException;
import org.springframework.data.redis.connection.stream.ObjectRecord;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.connection.stream.StreamRecords;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StreamOperations;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Primary
public class AuctionServiceImpl implements AuctionService {
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

    @Override
    public Long enrollAuction(AuctionEnrollRequest auctionEnrollRequest) {
        AuctionType auctionType = auctionEnrollRequest.getAuctionTypeEnum();
        if(auctionType == AuctionType.ERROR){
            return -1L;
        }
        ValueOperations vop = redisTemplate.opsForValue();
        long productId = auctionEnrollRequest.getProductId();

        Product product = productRepository.getOne(productId);
        product.setOnAuction(1);
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
            xAdd(auctionId, auctionId + "-0", auctionEvent);
        } catch (RedisSystemException e) {
            System.out.println("이미 등록쓰");
        }

        vop.set(AUCTION_TYPE_REDIS+auctionId,auctionType.getValue());

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
                .serverVersion(auctionEvents.get(auctionEvents.size() - 1).getVersion())
                .build();
    }

    @Override
    public AuctionEventsResponse eventAuction(Long auctionId, AuctionEventRequest auctionEventRequest) throws IOException {
        ValueOperations vop = redisTemplate.opsForValue();
        AuctionType auctionType = AuctionType.fromText((String) vop.get(AUCTION_TYPE_REDIS+auctionId));
        switch (auctionType) {
            case BASIC:
                return auctionEventService.basicAuctionEvent(auctionId,auctionEventRequest);
            case LIVE:
                return auctionEventService.liveAuctionEvent(auctionId, auctionEventRequest);
            case ERROR:
            default:
                return null;
        }
    }
    private void xAdd(long auctionId, String versionId, AuctionEvent auctionEvent) throws RedisSystemException {
        StreamOperations sop = redisTemplate.opsForStream();
        ObjectRecord<String, AuctionEvent> record = StreamRecords.newRecord()
                .in(AUCTION_EVENTS_REDIS + auctionId)
                .withId(versionId)
                .ofObject(auctionEvent);
        sop.add(record);
    }
}
