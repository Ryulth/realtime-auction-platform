package com.ryulth.auction.service.auction;

import com.ryulth.auction.domain.Auction;
import com.ryulth.auction.domain.Product;
import com.ryulth.auction.domain.User;
import com.ryulth.auction.pojo.model.AuctionEvent;
import com.ryulth.auction.pojo.request.AuctionEventRequest;
import com.ryulth.auction.pojo.response.AuctionEventsResponse;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface AuctionEventService {
    AuctionEventsResponse basicAuctionEvent(long auctionId, AuctionEventRequest auctionEventRequest, User user);

    AuctionEventsResponse liveAuctionEvent(long auctionId, AuctionEventRequest auctionEventRequest, User user);

    AuctionEventsResponse firstComeAuctionEvent(Auction auction, AuctionEventRequest auctionEventRequest, User user, Product product);

    List<AuctionEvent> getAuctionEvents(long auctionId);

    boolean endEvents(Auction auction);
}
