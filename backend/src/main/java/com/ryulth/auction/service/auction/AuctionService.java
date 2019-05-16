package com.ryulth.auction.service.auction;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.ryulth.auction.domain.Auction;
import com.ryulth.auction.domain.User;
import com.ryulth.auction.pojo.request.AuctionEnrollRequest;
import com.ryulth.auction.pojo.request.AuctionEventRequest;
import com.ryulth.auction.pojo.response.AuctionDataResponse;
import com.ryulth.auction.pojo.response.AuctionEventsResponse;
import com.ryulth.auction.pojo.response.AuctionListResponse;
import org.springframework.stereotype.Service;

import java.io.IOException;


@Service
public interface AuctionService {
    Auction enrollAuction(AuctionEnrollRequest auctionEnrollRequest, User user) throws IOException;

    AuctionListResponse getAllAuctions() throws JsonProcessingException;

    AuctionDataResponse getAuction(Long auctionId,User user);

    AuctionEventsResponse getAuctionEvents(Long auctionId);

    AuctionEventsResponse eventAuction(Long auctionId, AuctionEventRequest auctionEventRequest,User user) throws IOException;

    void endAuction();
}
