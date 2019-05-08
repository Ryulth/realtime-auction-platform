package com.ryulth.auction.service.auction;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.ryulth.auction.pojo.model.AuctionType;
import com.ryulth.auction.pojo.request.AuctionEnrollRequest;
import com.ryulth.auction.pojo.request.AuctionEventRequest;
import com.ryulth.auction.pojo.response.AuctionDataResponse;
import com.ryulth.auction.pojo.response.AuctionEventsResponse;
import com.ryulth.auction.pojo.response.AuctionListResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public interface AuctionService {
    String enrollAuction(AuctionEnrollRequest auctionEnrollRequest) throws IOException;

    AuctionListResponse getAllAuctions() throws JsonProcessingException;

    AuctionDataResponse getAuction(String auctionId);

    AuctionEventsResponse getAuctionEvents(String auctionId);

    AuctionEventsResponse eventAuction(String auctionId, AuctionEventRequest auctionEventRequest) throws IOException;

}
