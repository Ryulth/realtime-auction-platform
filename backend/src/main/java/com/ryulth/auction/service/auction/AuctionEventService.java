package com.ryulth.auction.service.auction;

import com.ryulth.auction.pojo.model.AuctionEventType;
import com.ryulth.auction.pojo.response.AuctionEventsResponse;
import org.springframework.stereotype.Service;

@Service
public interface AuctionEventService {
    String enrollAuction(Long productId);
    String auctionEvent(String auctionId, AuctionEventType auctionEventType);
    AuctionEventsResponse getAuctionEvents(String auctionId);
}
