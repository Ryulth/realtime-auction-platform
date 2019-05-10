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
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class BiddingService implements AuctionService {
    private static final Map<Long, AuctionEventData> biddingAuctionMap = new ConcurrentHashMap<>();
    private final static int SNAPSHOT_CYCLE = 100;
    @Autowired
    ProductRepository productRepository;
    @Autowired
    AuctionRepository auctionRepository;

    @Override
    public Long enrollAuction(AuctionEnrollRequest auctionEnrollRequest) {
        Long productId = auctionEnrollRequest.getProductId();
        if (auctionRepository.findByProductId(productId).size() > 0) {
            return -1L;
        }
        Product product = productRepository.getOne(productId);
        Auction auction = Auction.builder()
                .productId(productId)
                .auctionType(AuctionType.BIDDING.getValue())
                .startTime(product.getStartTime())
                .endTime(product.getEndTime())
                .price(product.getLowerLimit())
                .version(0L)
                .build();
        auctionRepository.save(auction);

        long auctionId = auction.getId();
        ArrayDeque<AuctionEvent> auctionEvents = new ArrayDeque<>();
        auctionEvents.add(AuctionEvent.builder()
                .auctionEventType(AuctionEventType.ENROLL)
                .version(0L)
                .price(product.getLowerLimit())
                .build());
        AuctionEventData auctionEventStreams = AuctionEventData.builder()
                .auctionId(auctionId)
                .auctionType(AuctionType.BIDDING)
                .startTime(product.getStartTime())
                .endTime(product.getEndTime())
                .product(product)
                .auctionEvents(auctionEvents)
                .build();
        synchronized (biddingAuctionMap) {
            biddingAuctionMap.put(auctionId, auctionEventStreams);
        }
        return auctionId;
    }

    @Override
    public AuctionListResponse getAllAuctions() {
        return null;
    }

    @Override
    public AuctionDataResponse getAuction(Long auctionId) {
        AuctionEventData auctionEventData;
        synchronized (biddingAuctionMap) {
            auctionEventData = biddingAuctionMap.get(auctionId);
        }
        return AuctionDataResponse.builder()
                .auctionEventData(auctionEventData)
                .build();
    }

    @Override
    public AuctionEventsResponse getAuctionEvents(Long auctionId) {
        ArrayDeque<AuctionEvent> auctionEvents;
        synchronized (biddingAuctionMap) {
            auctionEvents = biddingAuctionMap.get(auctionId).getAuctionEvents();
        }
        return AuctionEventsResponse.builder()
                .auctionEvents(auctionEvents)
                .serverVersion(auctionEvents.getLast().getVersion())
                .build();
    }

    @Override
    public AuctionEventsResponse eventAuction(Long auctionId, AuctionEventRequest auctionEventRequest) throws IOException {
        AuctionEventData auctionEventStreams;
        synchronized (biddingAuctionMap) {
            auctionEventStreams = biddingAuctionMap.get(auctionId);
        }
        if (auctionEventStreams == null) {
            return null;
        }
        ArrayDeque<AuctionEvent> auctionEvents = auctionEventStreams.getAuctionEvents();
        long serverVersion = auctionEvents.getLast().getVersion();
        long clientVersion = auctionEventRequest.getVersion();
        if (serverVersion == clientVersion) {
            auctionEvents.add(AuctionEvent.builder()
                    .auctionEventType(auctionEventRequest.getAuctionEventTypeEnum())
                    .version(serverVersion + 1)
                    .price(auctionEventRequest.getPrice())
                    .build());
            ArrayDeque<AuctionEvent> tempEvents = auctionEvents.clone();
            tempEvents.removeIf(e -> (e.getVersion() <= clientVersion));
            return AuctionEventsResponse.builder()
                    .auctionEvents(tempEvents)
                    .serverVersion(tempEvents.getLast().getVersion())
                    .build();
        }
        if (auctionEvents.getLast().getPrice() < auctionEventRequest.getPrice()) {
            auctionEvents.add(AuctionEvent.builder()
                    .auctionEventType(auctionEventRequest.getAuctionEventTypeEnum())
                    .version(serverVersion + 1)
                    .price(auctionEventRequest.getPrice())
                    .build());
            ArrayDeque<AuctionEvent> tempEvents = new ArrayDeque<>();
            tempEvents.add(auctionEvents.clone().getLast());
            return AuctionEventsResponse.builder()
                    .auctionEvents(tempEvents)
                    .serverVersion(tempEvents.getLast().getVersion())
                    .build();
        }
        return AuctionEventsResponse.builder()
                .auctionEvents(auctionEvents)
                .serverVersion(auctionEvents.getLast().getVersion())
                .build();
    }

    private long getSum(ArrayDeque<AuctionEvent> auctionEvents) {
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
