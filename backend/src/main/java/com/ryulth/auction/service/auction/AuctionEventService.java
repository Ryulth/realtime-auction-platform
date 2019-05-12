package com.ryulth.auction.service.auction;

import com.ryulth.auction.pojo.request.AuctionEventRequest;
import com.ryulth.auction.pojo.response.AuctionEventsResponse;
import org.springframework.stereotype.Service;

@Service
public interface AuctionEventService {
    AuctionEventsResponse basicAuctionEvent(long auctionId, AuctionEventRequest auctionEventRequest);
    AuctionEventsResponse liveAuctionEvent(long auctionId, AuctionEventRequest auctionEventRequest);
}
