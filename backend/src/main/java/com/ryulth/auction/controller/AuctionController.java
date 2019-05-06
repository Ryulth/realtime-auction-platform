package com.ryulth.auction.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.ryulth.auction.pojo.response.AuctionEventsResponse;
import com.ryulth.auction.pojo.response.AuctionListResponse;
import com.ryulth.auction.service.auction.AuctionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.file.Path;

@RestController
public class AuctionController {
    private static Logger logger = LoggerFactory.getLogger(AuctionController.class);
    @Autowired
    AuctionService auctionService;

    @GetMapping("/auctions")
    public ResponseEntity<AuctionListResponse> getAllAuctions() throws JsonProcessingException {
        return auctionService.getAllAuctions();
    }

    @GetMapping("/auctions/{auctionType}/{auctionId}/events")
    public ResponseEntity<AuctionEventsResponse> getAuctionEvents(
            @PathVariable("auctionType") String auctionType,
            @PathVariable("auctionId") String auctionId) throws JsonProcessingException {
        return auctionService.getAuctionEvents(auctionId,auctionType);
    }

    @PostMapping("/auction")
    public String enrollAuction(
            @RequestBody String payload) throws IOException {
        return auctionService.enrollAuction(payload);
    }

    @PostMapping("/auctions/{auctionType}/{auctionId}/event")
    public String eventAuction(
            @RequestBody String payload,
            @PathVariable("auctionType") String auctionType,
            @PathVariable("auctionId") String auctionId) throws IOException {
        return auctionService.eventAuction(auctionId,auctionType, payload);
    }
}
