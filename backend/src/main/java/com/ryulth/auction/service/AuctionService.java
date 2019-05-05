package com.ryulth.auction.service;

import org.springframework.stereotype.Service;

@Service
public interface AuctionService {
    void enrollAuction();
    String getAllAuctions();
}
