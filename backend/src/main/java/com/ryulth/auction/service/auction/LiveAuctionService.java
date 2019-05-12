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
import org.springframework.data.redis.RedisSystemException;
import org.springframework.data.redis.connection.stream.ObjectRecord;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.connection.stream.StreamRecords;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StreamOperations;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class LiveAuctionService implements AuctionService {
    //private static final Map<Long, AuctionEventData> competeAuctionMap = new ConcurrentHashMap<>();
    private final static int SNAPSHOT_CYCLE = 100;
    @Autowired
    ProductRepository productRepository;
    @Autowired
    AuctionRepository auctionRepository;
    @Autowired
    RedisTemplate redisTemplate;

    private static final String AUCTION_EVENTS_REDIS = "ryulth:auction:events:";

    @Override
    public Long enrollAuction(AuctionEnrollRequest auctionEnrollRequest) {
        return null;
    }

    @Override
    public AuctionListResponse getAllAuctions() {
        return null;
    }

    @Override
    public AuctionDataResponse getAuction(Long auctionId) {
//        AuctionEventData auctionEventData = competeAuctionMap.get(auctionId);
//        return AuctionDataResponse.builder()
//                .auctionEventData(auctionEventData)
//                .build();
        return null;
    }

    @Override
    public AuctionEventsResponse getAuctionEvents(Long auctionId) {
        return null;
    }

    @Override
    public AuctionEventsResponse eventAuction(Long auctionId, AuctionEventRequest auctionEventRequest) {
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
