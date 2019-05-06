package com.ryulth.auction.service.auction;

import com.ryulth.auction.domain.Product;
import com.ryulth.auction.pojo.model.AuctionEventStreams;
import com.ryulth.auction.pojo.model.AuctionEvent;
import com.ryulth.auction.pojo.model.AuctionEventType;
import com.ryulth.auction.pojo.model.AuctionType;
import com.ryulth.auction.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class BiddingEventService implements AuctionEventService {
    private static final Map<String, AuctionEventStreams> biddingAuctionMap = new HashMap<>();
    private final static int SNAPSHOT_CYCLE = 100;
    @Autowired
    ProductRepository productRepository;

    @Override
    public String enrollAuction(Long productId) {
        Product product = productRepository.getOne(productId);
        String auctionId = UUID.randomUUID().toString().replace("-", "");
        ArrayDeque<AuctionEvent> auctionEvents = new ArrayDeque<>();
        auctionEvents.add(AuctionEvent.builder()
                .auctionEventType(AuctionEventType.ENROLL)
                .version(0L)
                .price(product.getLowerLimit())
                .build());
        AuctionEventStreams auction = AuctionEventStreams.builder()
                .id(auctionId)
                .auctionType(AuctionType.BIDDING)
                .startTime(product.getStartTime())
                .endTime(product.getEndTime())
                .product(product)
                .auctionEvents(auctionEvents)
                .build();
        synchronized (biddingAuctionMap){
            biddingAuctionMap.put(auctionId,auction);
        }
        return "ENROLL BIDDING AUCTION " + auctionId;
    }

    @Override
    public String auctionEvent(String auctionId, AuctionEventType auctionEventType) {
        AuctionEventStreams auction;
        synchronized (biddingAuctionMap) {
            auction = biddingAuctionMap.get(auctionId);
        }
        if (auction == null) {
            return "FAIL";
        }
        ArrayDeque<AuctionEvent> auctionEvents = auction.getAuctionEvents();
        Long serverVersion = auctionEvents.getLast().getVersion();
        auctionEvents.add(AuctionEvent.builder()
                .auctionEventType(AuctionEventType.ENROLL)
                .version(serverVersion+1)
                .price(auctionEvents.getLast().getPrice()+1000) // TODO 임시로 1000원씩 입찰
                .build());
        return serverVersion.toString();
    }

    @Override
    public List<AuctionEventStreams> getAllAuctions() {
        List<AuctionEventStreams> auctions = new ArrayList<>();

        return null;
    }
}
