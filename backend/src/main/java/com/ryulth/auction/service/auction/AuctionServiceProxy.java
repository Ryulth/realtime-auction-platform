package com.ryulth.auction.service.auction;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.ryulth.auction.domain.Auction;
import com.ryulth.auction.pojo.model.AuctionType;
import com.ryulth.auction.pojo.request.AuctionEnrollRequest;
import com.ryulth.auction.pojo.request.AuctionEventRequest;
import com.ryulth.auction.pojo.response.AuctionDataResponse;
import com.ryulth.auction.pojo.response.AuctionEventsResponse;
import com.ryulth.auction.pojo.response.AuctionListResponse;
import com.ryulth.auction.repository.AuctionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Qualifier("auctionServiceProxy")
public class AuctionServiceProxy implements AuctionService {
    @Autowired
    BiddingService biddingService;
    @Autowired
    private AuctionRepository auctionRepository;
    private static final Map<Long,AuctionType> auctionTypeMap= new ConcurrentHashMap<>();
    private static final Map<Long ,Boolean> syncAuctions = new ConcurrentHashMap<>();
    private void checkSyncMap(Long auctionId) {
        if (syncAuctions.get(auctionId) == null) {
            syncAuctions.putIfAbsent(auctionId, true);
        }
    }
    @Override
    public Long enrollAuction(AuctionEnrollRequest auctionEnrollRequest) throws IOException {
        switch (auctionEnrollRequest.getAuctionTypeEnum()) {
            case BIDDING:
                auctionTypeMap.put(biddingService.enrollAuction(auctionEnrollRequest),auctionEnrollRequest.getAuctionTypeEnum());
                return 1L;
            case COMPETE:
            case ERROR:
            default:
                return -1L;
        }
    }

    @Override
    public AuctionListResponse getAllAuctions() {
        List<Auction> auctions = auctionRepository.findAll();
        return AuctionListResponse.builder().auctions(auctions).build();
    }

    @Override
    public AuctionDataResponse getAuction(Long auctionId) {
        AuctionType auctionType = auctionTypeMap.get(auctionId);
        switch (auctionType) {
            case BIDDING:
                return biddingService.getAuction(auctionId);
            case COMPETE:
            case ERROR:
            default:
                return null;
        }
    }

    @Override
    public AuctionEventsResponse getAuctionEvents(Long auctionId) {
        AuctionType auctionType = auctionTypeMap.get(auctionId);
        switch (auctionType) {
            case BIDDING:
                return biddingService.getAuctionEvents(auctionId);
            case COMPETE:
            case ERROR:
            default:
                return null;
        }
    }

    @Override
    public AuctionEventsResponse eventAuction(Long auctionId, AuctionEventRequest auctionEventRequest) throws IOException {
        AuctionType auctionType = auctionTypeMap.get(auctionId);
        switch (auctionType) {
            case BIDDING:
                return biddingService.eventAuction(auctionId,auctionEventRequest);
            case COMPETE:
            case ERROR:
            default:
                return null;
        }
    }
}
