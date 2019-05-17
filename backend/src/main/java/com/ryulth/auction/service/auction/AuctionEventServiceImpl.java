package com.ryulth.auction.service.auction;

import com.ryulth.auction.domain.Auction;
import com.ryulth.auction.domain.Product;
import com.ryulth.auction.domain.User;
import com.ryulth.auction.pojo.model.AuctionEvent;
import com.ryulth.auction.pojo.model.AuctionEventType;
import com.ryulth.auction.pojo.model.AuctionType;
import com.ryulth.auction.pojo.request.AuctionEventRequest;
import com.ryulth.auction.pojo.response.AuctionEventsResponse;
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
import org.springframework.stereotype.Component;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Primary
public class AuctionEventServiceImpl implements AuctionEventService {
    private static Logger logger = LoggerFactory.getLogger(AuctionEventServiceImpl.class);
    private final RedisTemplate redisTemplate;
    private final SimpMessagingTemplate simpMessagingTemplate;

    private static final ZoneId zoneId = ZoneId.of("Asia/Seoul");
    private static final String AUCTION_EVENTS_REDIS = "ryulth:auction:events:";
    private static final String AUCTION_ONGOING_REDIS = "ryulth:auction:ongoing:";

    @Autowired
    public AuctionEventServiceImpl(RedisTemplate redisTemplate,SimpMessagingTemplate simpMessagingTemplate) {
        this.redisTemplate = redisTemplate;
        this.simpMessagingTemplate = simpMessagingTemplate;
    }
    //TODO 중복 코드 제거

    @Override
    public AuctionEventsResponse basicAuctionEvent(long auctionId, AuctionEventRequest auctionEventRequest, User user) {
        List<AuctionEvent> auctionEvents = getAuctionEvents(auctionId);
        AuctionEvent lastAuctionEvent = auctionEvents.get(auctionEvents.size() - 1);
        ValueOperations vop = redisTemplate.opsForValue();
        boolean onGoing = (boolean) vop.get(AUCTION_ONGOING_REDIS + auctionId);
        if (onGoing && lastAuctionEvent.getPrice() < auctionEventRequest.getPrice()) {
            long newPrice = auctionEventRequest.getPrice();
            long newVersion = lastAuctionEvent.getVersion() + 1;
            AuctionEvent newAuctionEvent = AuctionEvent.builder()
                    .auctionEventType(auctionEventRequest.getAuctionEventTypeEnum())
                    .version(newVersion)
                    .price(auctionEventRequest.getPrice())
                    .userId(user.getId())
                    .nickName(user.getNickName())
                    .eventTime(ZonedDateTime.now(zoneId))
                    .build();
            try {
                xAdd(AUCTION_EVENTS_REDIS +auctionId, auctionId + "-" + newPrice, newAuctionEvent);
                List<AuctionEvent> tempEvents = new ArrayList<>();
                tempEvents.add(newAuctionEvent);
                logger.info("올바른 입력");
                return AuctionEventsResponse.builder()
                        .auctionType(AuctionType.BASIC.getValue())
                        .auctionEvents(tempEvents)
                        .build();
            } catch (RedisSystemException ignore) {
                logger.info("Redis 버전충돌");
            }

        }
        logger.info("버전충돌");
        return AuctionEventsResponse.builder()
                .auctionType(AuctionType.BASIC.getValue())
                .auctionEvents(auctionEvents)
                .build();
    }

    @Override
    public AuctionEventsResponse liveAuctionEvent(long auctionId, AuctionEventRequest auctionEventRequest, User user) {
        List<AuctionEvent> auctionEvents = getAuctionEvents(auctionId);
        AuctionEvent lastAuctionEvent = auctionEvents.get(auctionEvents.size() - 1);
        ValueOperations vop = redisTemplate.opsForValue();
        boolean onGoing = (boolean) vop.get(AUCTION_ONGOING_REDIS + auctionId);
        if (onGoing && lastAuctionEvent.getVersion() == auctionEventRequest.getVersion()) {
            long newPrice = auctionEventRequest.getPrice();
            long newVersion = lastAuctionEvent.getVersion() + 1;
            AuctionEvent newAuctionEvent = AuctionEvent.builder()
                    .auctionEventType(auctionEventRequest.getAuctionEventTypeEnum())
                    .version(newVersion)
                    .price(auctionEventRequest.getPrice())
                    .userId(user.getId())
                    .nickName(user.getNickName())
                    .eventTime(ZonedDateTime.now(zoneId))
                    .build();
            try {
                xAdd(AUCTION_EVENTS_REDIS +auctionId, auctionId + "-" + newVersion, newAuctionEvent);
                List<AuctionEvent> tempEvents = new ArrayList<>();
                tempEvents.add(newAuctionEvent);
                logger.info("올바른 입력");
                return AuctionEventsResponse.builder()
                        .auctionType(AuctionType.LIVE.getValue())
                        .auctionEvents(tempEvents)
                        .build();
            } catch (RedisSystemException ignore) {
                logger.info("Redis 버전충돌");
            }
        }
        logger.info("버전충돌");

        return AuctionEventsResponse.builder()
                .auctionType(AuctionType.LIVE.getValue())
                .auctionEvents(auctionEvents)
                .build();
    }

    @Override
    public AuctionEventsResponse firstComeAuctionEvent(Auction auction, AuctionEventRequest auctionEventRequest, User user , Product product) {
        ValueOperations vop = redisTemplate.opsForValue();
        long auctionId = auction.getId();

        boolean onGoing = (boolean) vop.get(AUCTION_ONGOING_REDIS + auctionId);
        long clientVersion = auctionEventRequest.getVersion();
        if (onGoing) {
            long newVersion = clientVersion;
            AuctionEvent newAuctionEvent = AuctionEvent.builder()
                    .auctionEventType(auctionEventRequest.getAuctionEventTypeEnum())
                    .version(newVersion + 1)
                    .price(auctionEventRequest.getPrice())
                    .userId(user.getId())
                    .nickName(user.getNickName())
                    .eventTime(ZonedDateTime.now(zoneId))
                    .build();
            while (true) {
                try {
                    xAdd(AUCTION_EVENTS_REDIS + auctionId, auctionId + "-" + newAuctionEvent.getVersion(), newAuctionEvent);
                    break;
                } catch (RedisSystemException ignore) {
                    newAuctionEvent.setVersion(newAuctionEvent.getVersion() + 1);
                }
            }
            if(getAuctionEvents(auctionId).size()>product.getCount()){
                vop.set(AUCTION_ONGOING_REDIS + auctionId,false);
                endEvents(auction);
            }
        }
        List<AuctionEvent> auctionEvents = getAuctionEvents(auctionId);
        if(clientVersion +1 ==auctionEvents.get(auctionEvents.size()-1).getVersion()){
            auctionEvents.removeIf(a -> (a.getVersion() <= clientVersion));
        }
        return AuctionEventsResponse.builder()
                .auctionType(AuctionType.FIRSTCOME.getValue())
                .auctionEvents(auctionEvents)
                .build();
    }

    @Override
    public List<AuctionEvent> getAuctionEvents(long auctionId) {
        StreamOperations sop = redisTemplate.opsForStream();
        List<ObjectRecord<String, AuctionEvent>> objectRecords = sop
                .read(AuctionEvent.class, StreamOffset.fromStart(AUCTION_EVENTS_REDIS + auctionId));
        List<AuctionEvent> auctionEvents = objectRecords.stream()
                .map(o -> o.getValue())
                .collect(Collectors.toList());
        return auctionEvents;
    }

    @Override
    public boolean endEvents(Auction auction) {
        AuctionEvent closeEvent = AuctionEvent.builder()
                .userId(auction.getUserId())
                .nickName("Finish")
                .auctionEventType(AuctionEventType.CLOSE)
                .version(9000000000000000000L) // 구백 이십경 ...
                .price(0L)
                .eventTime(ZonedDateTime.now(zoneId))
                .build();
        try {
            xAdd(AUCTION_EVENTS_REDIS + auction.getId(), auction.getId() + "-" + Long.MAX_VALUE, closeEvent);
            List<AuctionEvent> auctionEvents = new ArrayList<>();
            auctionEvents.add(closeEvent);
            this.simpMessagingTemplate.convertAndSend("/topic/auctions/" + auction.getId() + "/event",
                    AuctionEventsResponse.builder()
                            .auctionEvents(auctionEvents)
                            .auctionType(auction.getAuctionType())
                            .build()
            );
            return true;

        } catch (RedisSystemException ignore) {
        }
        return false;
    }


    private void xAdd(String key, String versionId, AuctionEvent auctionEvent) throws RedisSystemException {
        StreamOperations sop = redisTemplate.opsForStream();
        ObjectRecord<String, AuctionEvent> record = StreamRecords.newRecord()
                .in(key)
                .withId(versionId)
                .ofObject(auctionEvent);
        sop.add(record);
    }
}
