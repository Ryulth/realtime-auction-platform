package com.ryulth.auction.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ryulth.auction.pojo.request.AuctionEnrollRequest;
import com.ryulth.auction.pojo.request.AuctionEventRequest;
import com.ryulth.auction.pojo.response.AuctionDataResponse;
import com.ryulth.auction.pojo.response.AuctionEventsResponse;
import com.ryulth.auction.pojo.response.AuctionListResponse;
import com.ryulth.auction.service.auction.AuctionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
public class AuctionController {
    private static Logger logger = LoggerFactory.getLogger(AuctionController.class);
    @Autowired
    @Qualifier("auctionServiceProxy")
    AuctionService auctionService;
    @Autowired
    ObjectMapper objectMapper;
    private static final HttpHeaders httpHeaders = new HttpHeaders();

    @GetMapping("/auctions")
    public ResponseEntity<AuctionListResponse> getAllAuctions() throws JsonProcessingException {
        httpHeaders.setContentType(MediaType.APPLICATION_JSON_UTF8);
        return new ResponseEntity<>(auctionService.getAllAuctions(), httpHeaders, HttpStatus.OK);
    }

    @PostMapping("/auction")
    public Long enrollAuction(
            @RequestBody String payload) throws IOException {
        AuctionEnrollRequest auctionEnrollRequest = objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .readValue(payload, AuctionEnrollRequest.class);
        httpHeaders.setContentType(MediaType.APPLICATION_JSON_UTF8);
        return auctionService.enrollAuction(auctionEnrollRequest);
    }

    @GetMapping("/auctions/{auctionId}")
    public ResponseEntity<AuctionDataResponse> getAuction(
            @PathVariable("auctionId") Long auctionId) {
        httpHeaders.setContentType(MediaType.APPLICATION_JSON_UTF8);
        return new ResponseEntity<>(auctionService.getAuction(auctionId), httpHeaders, HttpStatus.OK);
    }

    @GetMapping("/auctions/{auctionId}/events")
    public ResponseEntity<AuctionEventsResponse> getAuctionEvents(
            @PathVariable("auctionId") Long auctionId) {
        httpHeaders.setContentType(MediaType.APPLICATION_JSON_UTF8);
        return new ResponseEntity<>(auctionService.getAuctionEvents(auctionId), httpHeaders, HttpStatus.OK);
    }

    @PostMapping("/auctions/{auctionId}/event")
    public ResponseEntity<AuctionEventsResponse> eventAuction(
            @RequestBody String payload,
            @PathVariable("auctionId") Long auctionId) throws IOException {
        AuctionEventRequest auctionEventRequest = objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .readValue(payload, AuctionEventRequest.class);
        httpHeaders.setContentType(MediaType.APPLICATION_JSON_UTF8);
        return new ResponseEntity<>(auctionService.eventAuction(auctionId, auctionEventRequest), httpHeaders, HttpStatus.OK);

    }
}
