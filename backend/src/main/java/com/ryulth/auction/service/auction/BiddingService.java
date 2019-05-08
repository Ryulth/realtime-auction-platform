package com.ryulth.auction.service.auction;

import com.fasterxml.jackson.core.JsonProcessingException;
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
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class BiddingService implements AuctionService {
    private static final Map<String, AuctionEventData> biddingAuctionMap = new ConcurrentHashMap<>();
    private final static int SNAPSHOT_CYCLE = 100;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private AuctionRepository auctionRepository;

    @Override
    public String enrollAuction(AuctionEnrollRequest auctionEnrollRequest) throws IOException {
        Long productId = auctionEnrollRequest.getProductId();
        if (auctionRepository.findByProductId(productId).size() > 0) {
            return "Already ENROLL";
        }
        Product product = productRepository.getOne(productId);
        String auctionId = UUID.randomUUID().toString().replace("-", "");
        ArrayDeque<AuctionEvent> auctionEvents = new ArrayDeque<>();
        auctionEvents.add(AuctionEvent.builder()
                .auctionEventType(AuctionEventType.ENROLL)
                .version(0L)
                .price(product.getLowerLimit())
                .build());
        AuctionEventData auctionEventStreams = AuctionEventData.builder()
                .id(auctionId)
                .auctionType(AuctionType.BIDDING)
                .startTime(product.getStartTime())
                .endTime(product.getEndTime())
                .product(product)
                .auctionEvents(auctionEvents)
                .build();
        synchronized (biddingAuctionMap) {
            biddingAuctionMap.put(auctionId, auctionEventStreams);
        }
        Auction auction = Auction.builder()
                .auctionId(auctionId)
                .productId(productId)
                .auctionType(AuctionType.BIDDING.getValue())
                .startTime(product.getStartTime())
                .endTime(product.getEndTime())
                .price(product.getLowerLimit())
                .version(0L)
                .build();
        auctionRepository.save(auction);
        return auctionId;
    }

    @Override
    public AuctionListResponse getAllAuctions() {
        return null;
    }

    @Override
    public AuctionDataResponse getAuction(String auctionId) {
        AuctionEventData auctionEventData;
        synchronized (biddingAuctionMap) {
            auctionEventData = biddingAuctionMap.get(auctionId);
        }
        return AuctionDataResponse.builder()
                .auctionEventData(auctionEventData)
                .build();
    }

    @Override
    public AuctionEventsResponse getAuctionEvents(String auctionId) {
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
    public AuctionEventsResponse eventAuction(String auctionId, AuctionEventRequest auctionEventRequest) throws IOException {
        AuctionEventData auctionEventStreams;
        synchronized (biddingAuctionMap) {
            auctionEventStreams = biddingAuctionMap.get(auctionId);
        }
        if (auctionEventStreams == null) {
            return null;
        }
        ArrayDeque<AuctionEvent> auctionEvents = auctionEventStreams.getAuctionEvents();
        Long serverVersion = auctionEvents.getLast().getVersion();
        Long clientVersion = auctionEventRequest.getVersion();
        if (serverVersion.equals(clientVersion)) {
            auctionEvents.add(AuctionEvent.builder()
                    .auctionEventType(auctionEventRequest.getAuctionEventTypeEnum())
                    .version(serverVersion + 1)
                    .price(auctionEventRequest.getPrice())//auctionEvents.getLast().getPrice()+1000) // TODO 임시로 1000원씩 입찰
                    .build());
            ArrayDeque<AuctionEvent> tempEvents = auctionEvents.clone();
            tempEvents.removeIf(e -> (e.getVersion() <= clientVersion));
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
}
