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
public class BasicAuctionService implements AuctionService {
    private static final Map<Long, AuctionEventData> biddingAuctionMap = new ConcurrentHashMap<>();
    private final static int SNAPSHOT_CYCLE = 100;
    @Autowired
    ProductRepository productRepository;
    @Autowired
    AuctionRepository auctionRepository;

    @Override
    public Long enrollAuction(AuctionEnrollRequest auctionEnrollRequest) {
        Long productId = auctionEnrollRequest.getProductId();

        Product product = productRepository.getOne(productId);
        product.setOnAuction(1);
        productRepository.save(product);

        Auction auction = auctionRepository.findByProductId(productId)
                .orElse(Auction.builder()
                        .productId(productId)
                        .auctionType(AuctionType.BASIC.getValue())
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
                .auctionType(AuctionType.BASIC)
                .startTime(product.getStartTime())
                .endTime(product.getEndTime())
                .product(product)
                .auctionEvents(auctionEvents)
                .build();
        biddingAuctionMap.put(auctionId, auctionEventStreams);
        return auctionId;
    }

    @Override
    public AuctionListResponse getAllAuctions() {
        return null;
    }

    @Override
    public AuctionDataResponse getAuction(Long auctionId) {
        AuctionEventData auctionEventData;
        auctionEventData = biddingAuctionMap.get(auctionId);
        return AuctionDataResponse.builder()
                .auctionEventData(auctionEventData)
                .build();
    }

    @Override
    public AuctionEventsResponse getAuctionEvents(Long auctionId) {
        Deque<AuctionEvent> auctionEvents = biddingAuctionMap.get(auctionId).getAuctionEvents();
        return AuctionEventsResponse.builder()
                .auctionEvents(auctionEvents)
                .serverVersion(auctionEvents.getLast().getVersion())
                .build();
    }

    @Override
    public AuctionEventsResponse eventAuction(Long auctionId, AuctionEventRequest auctionEventRequest) throws IOException {
        AuctionEventData auctionEventData;
        auctionEventData = biddingAuctionMap.get(auctionId);
        if (auctionEventData == null) {
            return null;
        }
        Deque<AuctionEvent> auctionEvents = auctionEventData.getAuctionEvents();
        synchronized (auctionEvents) {
            if (auctionEvents.getLast().getPrice() < auctionEventRequest.getPrice()) {
                long serverVersion = auctionEvents.getLast().getVersion();
                auctionEvents.add(AuctionEvent.builder()
                        .auctionEventType(auctionEventRequest.getAuctionEventTypeEnum())
                        .version(serverVersion + 1)
                        .price(auctionEventRequest.getPrice())
                        .build());
                Deque<AuctionEvent> tempEvents = new ArrayDeque<>();
                tempEvents.add(auctionEvents.getLast());
                return AuctionEventsResponse.builder()
                        .auctionType(AuctionType.BASIC.getValue())
                        .auctionEvents(tempEvents)
                        .serverVersion(tempEvents.getLast().getVersion())
                        .build();
            }
        }
        return AuctionEventsResponse.builder()
                .auctionType(AuctionType.BASIC.getValue())
                .auctionEvents(auctionEvents)
                .serverVersion(auctionEvents.getLast().getVersion())
                .build();
    }
}
