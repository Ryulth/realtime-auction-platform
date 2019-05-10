package com.ryulth.auction.service.auction;

import com.ryulth.auction.domain.Auction;
import com.ryulth.auction.domain.Product;
import com.ryulth.auction.pojo.model.AuctionEvent;
import com.ryulth.auction.pojo.model.AuctionEventData;
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
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class LiveAuctionService implements AuctionService {
    private static final Map<Long, AuctionEventData> competeAuctionMap = new ConcurrentHashMap<>();
    private final static int SNAPSHOT_CYCLE = 100;
    @Autowired
    ProductRepository productRepository;
    @Autowired
    AuctionRepository auctionRepository;

    @Override
    public Long enrollAuction(AuctionEnrollRequest auctionEnrollRequest) {
        long productId = auctionEnrollRequest.getProductId();

        Product product = productRepository.getOne(productId);
        product.setOnAuction(1);
        productRepository.save(product);
        Auction auction = auctionRepository.findByProductId(productId)
                .orElse(Auction.builder()
                        .productId(productId)
                        .auctionType(AuctionType.LIVE.getValue())
                        .startTime(product.getStartTime())
                        .endTime(product.getEndTime())
                        .price(product.getLowerLimit())
                        .version(0L)
                        .build());
        auctionRepository.save(auction);

        long auctionId = auction.getId();
        Deque<AuctionEvent> auctionEvents = new ArrayDeque<>();
        auctionEvents.add(AuctionEvent.builder()
                .auctionEventType(AuctionEventType.ENROLL)
                .version(0L)
                .price(product.getLowerLimit())
                .build());
        AuctionEventData auctionEventStreams = AuctionEventData.builder()
                .auctionId(auctionId)
                .auctionType(AuctionType.LIVE)
                .startTime(product.getStartTime())
                .endTime(product.getEndTime())
                .product(product)
                .auctionEvents(auctionEvents)
                .build();
        competeAuctionMap.put(auctionId, auctionEventStreams);
        return auctionId;
    }

    @Override
    public AuctionListResponse getAllAuctions() {
        return null;
    }

    @Override
    public AuctionDataResponse getAuction(Long auctionId) {
        AuctionEventData auctionEventData = competeAuctionMap.get(auctionId);
        return AuctionDataResponse.builder()
                .auctionEventData(auctionEventData)
                .build();
    }

    @Override
    public AuctionEventsResponse getAuctionEvents(Long auctionId) {
        Deque<AuctionEvent> auctionEvents = competeAuctionMap.get(auctionId).getAuctionEvents();
        return AuctionEventsResponse.builder()
                .auctionEvents(auctionEvents)
                .serverVersion(auctionEvents.getLast().getVersion())
                .build();
    }

    @Override
    public AuctionEventsResponse eventAuction(Long auctionId, AuctionEventRequest auctionEventRequest) throws IOException {
        AuctionEventData auctionEventStreams = competeAuctionMap.get(auctionId);
        if (auctionEventStreams == null) {
            return null;
        }
        Deque<AuctionEvent> auctionEvents = auctionEventStreams.getAuctionEvents();
        synchronized (auctionEvents) {
            long serverVersion = auctionEvents.getLast().getVersion();
            long clientVersion = auctionEventRequest.getVersion();
            if (serverVersion == clientVersion) {
                auctionEvents.add(AuctionEvent.builder()
                        .auctionEventType(auctionEventRequest.getAuctionEventTypeEnum())
                        .version(serverVersion + 1)
                        .price(auctionEventRequest.getPrice())
                        .build());
                //Deque<AuctionEvent> tempEvents = ((ArrayDeque<AuctionEvent>)auctionEvents).clone();
                //tempEvents.removeIf(e -> (e.getVersion() <= clientVersion));
                Deque<AuctionEvent> tempEvents = new ArrayDeque<>();
                tempEvents.add(auctionEvents.getLast());
                return AuctionEventsResponse.builder()
                        .auctionEvents(tempEvents)
                        .serverVersion(tempEvents.getLast().getVersion())
                        .build();
            }
        }

        return AuctionEventsResponse.builder()
                .auctionEvents(auctionEvents)
                .serverVersion(auctionEvents.getLast().getVersion())
                .build();
    }

    private long getSum(Deque<AuctionEvent> auctionEvents) {
        long sum = 0;
        for (AuctionEvent auctionEvent : auctionEvents) {
            if (auctionEvent.getAuctionEventType() == AuctionEventType.ERROR) {
                sum += auctionEvent.getPrice();
            }
            if (auctionEvent.getAuctionEventType() == AuctionEventType.BID) {
                sum += auctionEvent.getPrice();
            }
        }
        System.out.println("CURRENT SUM" + sum);
        return sum;
    }
}
