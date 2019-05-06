package com.ryulth.auction.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.ryulth.auction.service.auction.AuctionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
public class AuctionController {
    private static Logger logger = LoggerFactory.getLogger(AuctionController.class);
    @Autowired
    AuctionService auctionService;

    @GetMapping("/auctions")
    public String getAllAuctions() throws JsonProcessingException {
        return auctionService.getAllAuctions();
    }

    @PostMapping("/auction")
    public String enrollAuction(
            @RequestBody String payload) throws IOException {
        return auctionService.enrollAuction(payload);
    }
}
