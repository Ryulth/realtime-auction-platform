package com.ryulth.auction.service;

import org.springframework.stereotype.Component;

@Component
public class SimpleAuctionService implements AuctionService{

    @Override
    public void enrollAuction() {

    }

    @Override
    public String getAllAuctions() {
        return "Test";
    }
}
