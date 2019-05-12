package com.ryulth.auction.service.auction;

import com.ryulth.auction.pojo.model.AuctionEvent;
import com.ryulth.auction.pojo.model.AuctionType;
import com.ryulth.auction.pojo.request.AuctionEventRequest;
import com.ryulth.auction.pojo.response.AuctionEventsResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.RedisSystemException;
import org.springframework.data.redis.connection.stream.ObjectRecord;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.connection.stream.StreamRecords;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StreamOperations;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Primary
public class AuctionEventServiceImpl implements AuctionEventService{
    @Autowired
    RedisTemplate redisTemplate;

    private static final String AUCTION_EVENTS_REDIS = "ryulth:auction:events:";

    @Override
    public AuctionEventsResponse basicAuctionEvent(long auctionId, AuctionEventRequest auctionEventRequest) {
        StreamOperations sop = redisTemplate.opsForStream();
        List<ObjectRecord<String, AuctionEvent>> auctionEvents = sop
                .read(AuctionEvent.class, StreamOffset.fromStart(AUCTION_EVENTS_REDIS + auctionId));
        AuctionEvent lastAuctionEvent = auctionEvents.get(auctionEvents.size() - 1).getValue();
        if (lastAuctionEvent.getPrice() < auctionEventRequest.getPrice()) {
            long newPrice = auctionEventRequest.getPrice();
            long newVersion = lastAuctionEvent.getVersion() + 1;
            AuctionEvent newAuctionEvent = AuctionEvent.builder()
                    .auctionEventType(auctionEventRequest.getAuctionEventTypeEnum())
                    .version(newVersion)
                    .price(auctionEventRequest.getPrice())
                    .build();
            try {
                xAdd(auctionId, auctionId + "-" + newPrice, newAuctionEvent);
            } catch (RedisSystemException e) {
                System.out.println("REDIS 에서 가격 충돌쓰");
            }
            System.out.println("올바른 가격띠");
            List<AuctionEvent> tempEvents = new ArrayList<>();
            tempEvents.add(newAuctionEvent);
            return AuctionEventsResponse.builder()
                    .auctionType(AuctionType.LIVE.getValue())
                    .auctionEvents(tempEvents)
                    .serverVersion(newVersion)
                    .build();
        }
        List<AuctionEvent> tempEvents = auctionEvents.stream()
                .map(o -> o.getValue())
                .collect(Collectors.toList());
        System.out.println("가격충돌");
        return AuctionEventsResponse.builder()
                .auctionType(AuctionType.LIVE.getValue())
                .auctionEvents(tempEvents)
                .serverVersion(lastAuctionEvent.getVersion())
                .build();
    }

    @Override
    public AuctionEventsResponse liveAuctionEvent(long auctionId, AuctionEventRequest auctionEventRequest) {
        StreamOperations sop = redisTemplate.opsForStream();
        List<ObjectRecord<String, AuctionEvent>> auctionEvents = sop
                .read(AuctionEvent.class, StreamOffset.fromStart(AUCTION_EVENTS_REDIS + auctionId));
        AuctionEvent lastAuctionEvent = auctionEvents.get(auctionEvents.size() - 1).getValue();
        if (lastAuctionEvent.getVersion() == auctionEventRequest.getVersion()) {
            long newPrice = auctionEventRequest.getPrice();
            long newVersion = lastAuctionEvent.getVersion() + 1;
            AuctionEvent newAuctionEvent = AuctionEvent.builder()
                    .auctionEventType(auctionEventRequest.getAuctionEventTypeEnum())
                    .version(newVersion)
                    .price(auctionEventRequest.getPrice())
                    .build();
            try {
                xAdd(auctionId, auctionId + "-" + newVersion, newAuctionEvent);
            } catch (RedisSystemException e) {
                System.out.println("REDIS 에서 버전충돌쓰");
            }
            System.out.println("올바른 버젼띠");
            List<AuctionEvent> tempEvents = new ArrayList<>();
            tempEvents.add(newAuctionEvent);
            return AuctionEventsResponse.builder()
                    .auctionType(AuctionType.LIVE.getValue())
                    .auctionEvents(tempEvents)
                    .serverVersion(newVersion)
                    .build();
        }

        List<AuctionEvent> tempEvents = auctionEvents.stream()
                .map(o -> o.getValue())
                .collect(Collectors.toList());
        System.out.println("버전충돌");
        return AuctionEventsResponse.builder()
                .auctionType(AuctionType.LIVE.getValue())
                .auctionEvents(tempEvents)
                .serverVersion(lastAuctionEvent.getVersion())
                .build();
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
