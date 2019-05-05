package com.ryulth.auction.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuctionController {
    private static Logger logger = LoggerFactory.getLogger(AuctionController.class);

    @GetMapping("/auction/products")
    public String getAuctions(){
        return "test";
    }
    @PostMapping("/auction/product")
    public void postAuction(@RequestBody String reqBody){
        logger.info("POST product",reqBody);
    }

}
