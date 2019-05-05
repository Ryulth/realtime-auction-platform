package com.ryulth.auction.controller;

import com.ryulth.auction.service.AuctionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuctionController {
    private static Logger logger = LoggerFactory.getLogger(AuctionController.class);
    @Autowired
    AuctionService auctionService;

    @GetMapping("/auctions")
    public String getAllAuctions(){
        return auctionService.getAllAuctions();
    }

    @PostMapping("/auction")
    public void postAuction(@RequestBody String reqBody){
        logger.info("POST pAuction",reqBody);
        auctionService.enrollAuction();
    }

}
