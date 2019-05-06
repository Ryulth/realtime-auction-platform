package com.ryulth.auction.service.auction;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public interface AuctionService {
    String enrollAuction(String payload) throws IOException;
    String getAllAuctions() throws JsonProcessingException;
}
