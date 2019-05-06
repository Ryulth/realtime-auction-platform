package com.ryulth.auction.service.auction;

import com.ryulth.auction.domain.Auction;
import com.ryulth.auction.domain.Product;
import com.ryulth.auction.pojo.model.AuctionEventStreams;
import com.ryulth.auction.pojo.model.AuctionEvent;
import com.ryulth.auction.pojo.model.AuctionEventType;
import com.ryulth.auction.pojo.model.AuctionType;
import com.ryulth.auction.pojo.response.AuctionEventsResponse;
import com.ryulth.auction.repository.AuctionRepository;
import com.ryulth.auction.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class BiddingEventService implements AuctionEventService {
    private static final Map<String, AuctionEventStreams> biddingAuctionMap = new HashMap<>();
    private final static int SNAPSHOT_CYCLE = 100;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private AuctionRepository auctionRepository;
    @Override
    public String enrollAuction(Long productId) {
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
    public String auctionEvent(String auctionId, AuctionEventType auctionEventType) {
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

    @Override
    public AuctionEventsResponse getAuctionEvents(String auctionId) {
        AuctionEventStreams auctionEventStreams;
        synchronized (biddingAuctionMap) {
            auctionEventStreams = biddingAuctionMap.get(auctionId);
        }
        return AuctionEventsResponse.builder().auctionEventStreams(auctionEventStreams).build();
    }

}
