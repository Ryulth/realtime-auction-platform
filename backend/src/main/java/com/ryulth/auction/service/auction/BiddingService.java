package com.ryulth.auction.service.auction;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.ryulth.auction.domain.Auction;
import com.ryulth.auction.domain.Product;
import com.ryulth.auction.pojo.model.AuctionEvent;
import com.ryulth.auction.pojo.model.AuctionEventStreams;
import com.ryulth.auction.pojo.model.AuctionEventType;
import com.ryulth.auction.pojo.model.AuctionType;
import com.ryulth.auction.pojo.request.AuctionEnrollRequest;
import com.ryulth.auction.pojo.response.AuctionEventsResponse;
import com.ryulth.auction.pojo.response.AuctionListResponse;
import com.ryulth.auction.repository.AuctionRepository;
import com.ryulth.auction.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class BiddingService implements AuctionService {
    private static final Map<String, AuctionEventStreams> biddingAuctionMap = new HashMap<>();
    private final static int SNAPSHOT_CYCLE = 100;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private AuctionRepository auctionRepository;

    @Override
    public String enrollAuction(AuctionEnrollRequest auctionEnrollRequest) throws IOException {
        Long productId = auctionEnrollRequest.getProductId();
        if(auctionRepository.findByProductId(productId).size() > 0){
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
        AuctionEventStreams auctionEventStreams = AuctionEventStreams.builder()
                .id(auctionId)
                .auctionType(AuctionType.BIDDING)
                .startTime(product.getStartTime())
                .endTime(product.getEndTime())
                .product(product)
                .auctionEvents(auctionEvents)
                .build();
        synchronized (biddingAuctionMap){
            biddingAuctionMap.put(auctionId,auctionEventStreams);
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
        return "ENROLL BIDDING AUCTION " + auctionId;
    }

    @Override
    public AuctionListResponse getAllAuctions() throws JsonProcessingException {
        return null;
    }

    @Override
    public AuctionEventsResponse getAuctionEvents(String auctionId, AuctionType auctionType) {
        AuctionEventStreams auctionEventStreams;
        synchronized (biddingAuctionMap) {
            auctionEventStreams = biddingAuctionMap.get(auctionId);
        }
        return AuctionEventsResponse.builder().auctionEventStreams(auctionEventStreams).build();
    }

    @Override
    public String eventAuction(String auctionId, String auctionType, String payload) throws IOException {
        AuctionEventStreams auctionEventStreams;
        synchronized (biddingAuctionMap) {
            auctionEventStreams = biddingAuctionMap.get(auctionId);
        }
        if (auctionEventStreams == null) {
            return "FAIL";
        }
        ArrayDeque<AuctionEvent> auctionEvents = auctionEventStreams.getAuctionEvents();
        Long serverVersion = auctionEvents.getLast().getVersion();
        auctionEvents.add(AuctionEvent.builder()
                .auctionEventType(AuctionEventType.ENROLL)
                .version(serverVersion+1)
                .price(auctionEvents.getLast().getPrice()+1000) // TODO 임시로 1000원씩 입찰
                .build());
        return serverVersion.toString();
    }
}
