package com.ryulth.auction.service.auction;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.ryulth.auction.pojo.request.AuctionEnrollRequest;
import com.ryulth.auction.pojo.request.AuctionEventRequest;
import com.ryulth.auction.pojo.response.AuctionDataResponse;
import com.ryulth.auction.pojo.response.AuctionEventsResponse;
import com.ryulth.auction.pojo.response.AuctionListResponse;
import org.springframework.stereotype.Service;

import java.io.IOException;


@Service
public interface AuctionService {
    Long enrollAuction(AuctionEnrollRequest auctionEnrollRequest) throws IOException;

    AuctionListResponse getAllAuctions() throws JsonProcessingException;

    AuctionDataResponse getAuction(Long auctionId);

    AuctionEventsResponse getAuctionEvents(Long auctionId);

    AuctionEventsResponse eventAuction(Long auctionId, AuctionEventRequest auctionEventRequest) throws IOException;

    void endAuction();
}
