package com.ryulth.auction.service.auction;

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
    BiddingAuctionService biddingAuctionService;
    @Autowired
    CompeteAuctionService competeAuctionService;
    @Autowired
    AuctionRepository auctionRepository;
    //TODO Type map to redis cache
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
                auctionTypeMap.put(biddingAuctionService.enrollAuction(auctionEnrollRequest),auctionEnrollRequest.getAuctionTypeEnum());
                return 1L;
            case COMPETE:
                auctionTypeMap.put(competeAuctionService.enrollAuction(auctionEnrollRequest),auctionEnrollRequest.getAuctionTypeEnum());
                return 2L;
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
                return biddingAuctionService.getAuction(auctionId);
            case COMPETE:
                return competeAuctionService.getAuction(auctionId);
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
                return biddingAuctionService.getAuctionEvents(auctionId);
            case COMPETE:
                return competeAuctionService.getAuctionEvents(auctionId);
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
                return biddingAuctionService.eventAuction(auctionId,auctionEventRequest);
            case COMPETE:
                return competeAuctionService.eventAuction(auctionId,auctionEventRequest);
            case ERROR:
            default:
                return null;
        }
    }
}
