package com.ryulth.auction.service.auction;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.ryulth.auction.pojo.response.AuctionEventsResponse;
import com.ryulth.auction.pojo.response.AuctionListResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public interface AuctionService {
    String enrollAuction(String payload) throws IOException;

    ResponseEntity<AuctionListResponse> getAllAuctions() throws JsonProcessingException;

    ResponseEntity<AuctionEventsResponse> getAuctionEvents(String auctionId, String auctionType);

    String eventAuction(String auctionId, String auctionType, String payload) throws IOException;

}
