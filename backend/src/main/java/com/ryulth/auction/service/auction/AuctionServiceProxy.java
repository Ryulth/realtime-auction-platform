package com.ryulth.auction.service.auction;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.ryulth.auction.domain.Auction;
import com.ryulth.auction.pojo.model.AuctionType;
import com.ryulth.auction.pojo.request.AuctionEnrollRequest;
import com.ryulth.auction.pojo.response.AuctionEventsResponse;
import com.ryulth.auction.pojo.response.AuctionListResponse;
import com.ryulth.auction.repository.AuctionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

@Component
@Qualifier("auctionServiceProxy")
public class AuctionServiceProxy implements AuctionService {
    @Autowired
    BiddingService biddingService;
    @Autowired
    private AuctionRepository auctionRepository;
    @Override
    public String enrollAuction(AuctionEnrollRequest auctionEnrollRequest) throws IOException {
        switch (auctionEnrollRequest.getAuctionTypeEnum()) {
            case BIDDING:
                return biddingService.enrollAuction(auctionEnrollRequest);
            case COMPETE:
            case ERROR:
            default:
                return "NOT YET ENROLL";
        }
    }

    @Override
    public AuctionListResponse getAllAuctions() throws JsonProcessingException {
        List<Auction> auctions = auctionRepository.findAll();
        return AuctionListResponse.builder().auctions(auctions).build();
    }

    @Override
    public AuctionEventsResponse getAuctionEvents(String auctionId, AuctionType auctionType) {
        switch (auctionType) {
            case BIDDING:
                return biddingService.getAuctionEvents(auctionId,auctionType);
            case COMPETE:
            case ERROR:
            default:
                return null;
        }
    }

    @Override
    public String eventAuction(String auctionId, String auctionType, String payload) throws IOException {
        return null;
    }
}
