package com.ryulth.auction.service.auction;

import com.ryulth.auction.domain.Auction;
import com.ryulth.auction.domain.Product;
import com.ryulth.auction.domain.User;
import com.ryulth.auction.pojo.model.AuctionData;
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
import com.ryulth.auction.repository.UserRepository;
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
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Primary
public class AuctionServiceImpl implements AuctionService {
    private static Logger logger = LoggerFactory.getLogger(AuctionServiceImpl.class);
    private final AuctionRepository auctionRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final RedisTemplate redisTemplate;
    private final AuctionEventService auctionEventService;

    private static final ZoneId zoneId = ZoneId.of("Asia/Seoul");
    private static final String timePattern = "yyyy-MM-dd HH:mm";
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern(timePattern).withZone(zoneId);//;
    private static final String AUCTION_TYPE_REDIS = "ryulth:auction:type:";
    private static final String AUCTION_EVENTS_REDIS = "ryulth:auction:events:";
    private static final String AUCTION_ONGOING_REDIS = "ryulth:auction:ongoing:";

    @Autowired
    public AuctionServiceImpl(AuctionRepository auctionRepository, ProductRepository productRepository, UserRepository userRepository, RedisTemplate redisTemplate, AuctionEventService auctionEventService) {
        this.auctionRepository = auctionRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
        this.redisTemplate = redisTemplate;
        this.auctionEventService = auctionEventService;
    }

    @Override
    public Auction enrollAuction(AuctionEnrollRequest auctionEnrollRequest, User user) {
        AuctionType auctionType = auctionEnrollRequest.getAuctionTypeEnum();
        if (auctionType == AuctionType.ERROR) {
            return null;
        }
        ValueOperations vop = redisTemplate.opsForValue();
        long productId = auctionEnrollRequest.getProductId();

        Product product = productRepository.getOne(productId);
        product.setOnSale(1);
        productRepository.save(product);
        ZonedDateTime now = ZonedDateTime.now(zoneId);
        Auction auction = auctionRepository.findByProductId(productId)
                .orElse(Auction.builder()
                        .userId(user.getId())
                        .productId(productId)
                        .auctionType(auctionType.getValue())
                        .startTime(now)
                        .endTime(now.plusMinutes(auctionEnrollRequest.getGoingTime()))
                        .price(product.getLowerLimit())
                        .version(0L)
                        .build());
        auctionRepository.save(auction);
        System.out.println(auction);
        long auctionId = auction.getId();
        AuctionEvent auctionEvent = AuctionEvent.builder()
                .userId(user.getId())
                .nickName(user.getNickName())
                .auctionEventType(AuctionEventType.ENROLL)
                .version(0L)
                .price(product.getLowerLimit())
                .eventTime(ZonedDateTime.now(zoneId))
                .build();

        try {
            xAdd(AUCTION_EVENTS_REDIS + auctionId, auctionId + "-0", auctionEvent);
            vop.set(AUCTION_ONGOING_REDIS + auctionId, true);
        } catch (RedisSystemException e) {
            logger.info("이미 등록되있음");
        }

        vop.set(AUCTION_TYPE_REDIS + auctionId, auctionType.getValue());

        return auction;
    }

    @Override
    public AuctionListResponse getAllAuctions() {
        List<Auction> auctions = auctionRepository.findAll();
        List<AuctionData> auctionData = new ArrayList<>();

        for (Auction auction : auctions) {
            User user = userRepository.getOne(auction.getUserId());
            Product product = productRepository.getOne(auction.getProductId());
            auctionData.add(AuctionData.builder()
                    .auctionId(auction.getId())
                    .nickName(user.getNickName())
                    .product(product)
                    .build());
        }
        return AuctionListResponse.builder().auctions(auctionData).build();
    }

    @Override
    public AuctionDataResponse getAuction(Long auctionId, User user) {
        Auction auction = auctionRepository.findById(auctionId).orElse(null);
        Product product = productRepository.findById(auction.getProductId()).orElse(null);
        return AuctionDataResponse.builder()
                .userId(user.getId())
                .auctionEvents(getAuctionEvents(auctionId).getAuctionEvents())
                .auction(auction)
                .product(product)
                .build();
    }

    @Override
    public AuctionEventsResponse getAuctionEvents(Long auctionId) {
        List<AuctionEvent> auctionEvents = auctionEventService.getAuctionEvents(auctionId);
        return AuctionEventsResponse.builder()
                .auctionEvents(auctionEvents)
                .build();
    }

    @Override
    public AuctionEventsResponse eventAuction(Long auctionId, AuctionEventRequest auctionEventRequest, User user) throws IOException {
        ValueOperations vop = redisTemplate.opsForValue();
        AuctionType auctionType = AuctionType.fromText((String) vop.get(AUCTION_TYPE_REDIS + auctionId));

        switch (auctionType) {
            case BASIC:
                return auctionEventService.basicAuctionEvent(auctionId, auctionEventRequest, user);
            case LIVE:
                return auctionEventService.liveAuctionEvent(auctionId, auctionEventRequest, user);
            case FIRSTCOME:
                Auction auction = auctionRepository.getOne(auctionId);
                Product product = productRepository.getOne(auction.getProductId());
                return auctionEventService.firstComeAuctionEvent(auction,auctionEventRequest,user,product);
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
            logger.info(a.getId() + "종료");
            a.setOnAuction(0);
            auctionRepository.save(a);
            ValueOperations vop = redisTemplate.opsForValue();
            vop.set(AUCTION_ONGOING_REDIS + a.getId(), false);
            auctionEventService.endEvents(a);

        });

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
