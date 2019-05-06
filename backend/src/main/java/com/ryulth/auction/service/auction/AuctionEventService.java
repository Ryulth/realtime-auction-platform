package com.ryulth.auction.service.auction;

import com.ryulth.auction.pojo.model.AuctionEventStreams;
import com.ryulth.auction.pojo.model.AuctionEventType;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface AuctionEventService {
    String enrollAuction(Long productId);
    String auctionEvent(String AuctionId, AuctionEventType auctionEventType);
    List<AuctionEventStreams> getAllAuctions();
}
