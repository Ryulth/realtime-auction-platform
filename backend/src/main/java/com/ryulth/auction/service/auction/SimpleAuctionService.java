package com.ryulth.auction.service.auction;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ryulth.auction.pojo.request.AuctionEnrollRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

@Component
public class SimpleAuctionService implements AuctionService {
    @Autowired
    ObjectMapper objectMapper;
    @Autowired
    private Map<String, AuctionEventService> auctionEventServiceMap;
    @Override
    public String enrollAuction(String payload) throws IOException {
        AuctionEnrollRequest auctionEnrollRequest = objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .readValue(payload, AuctionEnrollRequest.class);
        String key = auctionEnrollRequest.getAuctionType() + "EventService";
        return auctionEventServiceMap.get(key).enrollAuction(auctionEnrollRequest.getProductId());
    }

    @Override
    public String getAllAuctions() throws JsonProcessingException {

        return "ALL AUCTION";
    }
}