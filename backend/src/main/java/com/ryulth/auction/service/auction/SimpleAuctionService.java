package com.ryulth.auction.service.auction;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ryulth.auction.domain.Auction;
import com.ryulth.auction.domain.Product;
import com.ryulth.auction.pojo.model.AuctionType;
import com.ryulth.auction.pojo.request.AuctionEnrollRequest;
import com.ryulth.auction.pojo.request.AuctionEventRequest;
import com.ryulth.auction.pojo.response.AuctionEventsResponse;
import com.ryulth.auction.pojo.response.AuctionListResponse;
import com.ryulth.auction.pojo.response.ProductListResponse;
import com.ryulth.auction.repository.AuctionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Component
public class SimpleAuctionService implements AuctionService {
    private static final HttpHeaders httpHeaders = new HttpHeaders();
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private Map<String, AuctionEventService> auctionEventServiceMap;
    @Autowired
    private AuctionRepository auctionRepository;

    @Override
    public String enrollAuction(String payload) throws IOException {
        AuctionEnrollRequest auctionEnrollRequest = objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .readValue(payload, AuctionEnrollRequest.class);
        return auctionEventServiceMap.get(auctionEnrollRequest.getAuctionTypeKey()).enrollAuction(auctionEnrollRequest.getProductId());
    }

    @Override
    public ResponseEntity<AuctionListResponse> getAllAuctions() throws JsonProcessingException {
        List<Auction> auctions = auctionRepository.findAll();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON_UTF8);
        return new ResponseEntity<>(AuctionListResponse.builder().auctions(auctions).build(),httpHeaders, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<AuctionEventsResponse> getAuctionEvents(String auctionId, String auctionType) {
        String key = auctionType + "EventService";
        AuctionEventsResponse auctionEvents= auctionEventServiceMap.get(key).getAuctionEvents(auctionId);
        httpHeaders.setContentType(MediaType.APPLICATION_JSON_UTF8);
        return new ResponseEntity<>(auctionEvents,httpHeaders, HttpStatus.OK);
    }

    @Override
    public String eventAuction(String auctionId, String auctionType, String payload) throws IOException {
        AuctionEventRequest auctionEventRequest = objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .readValue(payload, AuctionEventRequest.class);
        String key = auctionType + "EventService";
        return auctionEventServiceMap.get(key)
                .auctionEvent(auctionId,auctionEventRequest.getAuctionEventTypeEnum());
    }
}