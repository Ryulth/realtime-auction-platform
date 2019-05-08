package com.ryulth.auction.service.auction;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.ryulth.auction.pojo.model.AuctionType;
import com.ryulth.auction.pojo.request.AuctionEnrollRequest;
import com.ryulth.auction.pojo.request.AuctionEventRequest;
import com.ryulth.auction.pojo.response.AuctionEventsResponse;
import com.ryulth.auction.pojo.response.AuctionListResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public interface AuctionService {
    String enrollAuction(AuctionEnrollRequest auctionEnrollRequest) throws IOException;

    AuctionListResponse getAllAuctions() throws JsonProcessingException;

    AuctionEventsResponse getAuctionEvents(String auctionId, AuctionType auctionType);

    String eventAuction(String auctionId, AuctionType auctionType, AuctionEventRequest auctionEventRequest) throws IOException;

}
