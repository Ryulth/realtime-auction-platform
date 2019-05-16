package com.ryulth.auction.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ryulth.auction.domain.Auction;
import com.ryulth.auction.domain.User;
import com.ryulth.auction.pojo.request.AuctionEnrollRequest;
import com.ryulth.auction.pojo.request.AuctionEventRequest;
import com.ryulth.auction.pojo.response.AuctionDataResponse;
import com.ryulth.auction.pojo.response.AuctionEventsResponse;
import com.ryulth.auction.pojo.response.AuctionListResponse;
import com.ryulth.auction.service.account.AccountService;
import com.ryulth.auction.service.auction.AuctionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
public class AuctionController {
    private static Logger logger = LoggerFactory.getLogger(AuctionController.class);
    @Autowired
    SimpMessagingTemplate simpMessagingTemplate;
    @Autowired
    AuctionService auctionService;
    @Autowired
    ObjectMapper objectMapper;
    @Autowired
    AccountService accountService;

    private static final HttpHeaders httpHeaders = new HttpHeaders();
    public AuctionController(){
        httpHeaders.setContentType(MediaType.APPLICATION_JSON_UTF8);
    }
    @CrossOrigin("*")
    @GetMapping("/auctions")
    public ResponseEntity<AuctionListResponse> getAllAuctions(
    ) throws JsonProcessingException {
        return new ResponseEntity<>(auctionService.getAllAuctions(), httpHeaders, HttpStatus.OK);
    }

    @CrossOrigin("*")
    @PostMapping("/auction")
    public ResponseEntity<Auction> enrollAuction(
            @RequestHeader("Authorization") String token,
            @RequestBody String payload) throws IOException {
        User user = accountService.getUser(token);
        AuctionEnrollRequest auctionEnrollRequest = objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .readValue(payload, AuctionEnrollRequest.class);
        return new ResponseEntity<>(auctionService.enrollAuction(auctionEnrollRequest,user), httpHeaders, HttpStatus.OK);
    }

    @CrossOrigin("*")
    @GetMapping("/auctions/{auctionId}")
    public ResponseEntity<AuctionDataResponse> getAuction(
            @RequestHeader("Authorization") String token,
            @PathVariable("auctionId") Long auctionId) {
        User user = accountService.getUser(token);
        return new ResponseEntity<>(auctionService.getAuction(auctionId,user), httpHeaders, HttpStatus.OK);
    }

    @CrossOrigin("*")
    @GetMapping("/auctions/{auctionId}/events")
    public ResponseEntity<AuctionEventsResponse> getAuctionEvents(
            @PathVariable("auctionId") Long auctionId) {
        return new ResponseEntity<>(auctionService.getAuctionEvents(auctionId), httpHeaders, HttpStatus.OK);
    }

    @CrossOrigin("*")
    @PostMapping("/auctions/{auctionId}/event")
    public void eventAuction(
            @RequestHeader("Authorization") String token,
            @RequestBody String payload,
            @PathVariable("auctionId") Long auctionId) throws IOException {
        User user = accountService.getUser(token);
        AuctionEventRequest auctionEventRequest = objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .readValue(payload, AuctionEventRequest.class);
        this.simpMessagingTemplate.convertAndSend("/topic/auctions/"+auctionId+"/event",
                auctionService.eventAuction(auctionId, auctionEventRequest,user));
    }
}
