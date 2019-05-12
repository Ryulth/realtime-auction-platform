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
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Primary
public class AuctionServiceProxy implements AuctionService {
    @Autowired
    BasicAuctionService basicAuctionService;
    @Autowired
    LiveAuctionService liveAuctionService;
    @Autowired
    AuctionRepository auctionRepository;
    @Autowired
    RedisTemplate redisTemplate;

    private static final String AUCTION_TYPE_REDIS = "ryulth:auction:type:";


    @Override
    public Long enrollAuction(AuctionEnrollRequest auctionEnrollRequest) {
        ValueOperations vop = redisTemplate.opsForValue();
        switch (auctionEnrollRequest.getAuctionTypeEnum()) {
            case BASIC:
                vop.set(AUCTION_TYPE_REDIS+ basicAuctionService.enrollAuction(auctionEnrollRequest),
                        auctionEnrollRequest.getAuctionType());
                return 1L;
            case LIVE:
                vop.set(AUCTION_TYPE_REDIS+liveAuctionService.enrollAuction(auctionEnrollRequest),
                        auctionEnrollRequest.getAuctionType());
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
        ValueOperations vop = redisTemplate.opsForValue();
        AuctionType auctionType = AuctionType.fromText((String) vop.get(AUCTION_TYPE_REDIS+auctionId));
        switch (auctionType) {
            case BASIC:
                return basicAuctionService.getAuction(auctionId);
            case LIVE:
                return liveAuctionService.getAuction(auctionId);
            case ERROR:
            default:
                return null;
        }
    }

    @Override
    public AuctionEventsResponse getAuctionEvents(Long auctionId) {
        ValueOperations vop = redisTemplate.opsForValue();
        AuctionType auctionType = AuctionType.fromText((String) vop.get(AUCTION_TYPE_REDIS+auctionId));
        switch (auctionType) {
            case BASIC:
                return basicAuctionService.getAuctionEvents(auctionId);
            case LIVE:
                return liveAuctionService.getAuctionEvents(auctionId);
            case ERROR:
            default:
                return null;
        }
    }

    @Override
    public AuctionEventsResponse eventAuction(Long auctionId, AuctionEventRequest auctionEventRequest) throws IOException {
        ValueOperations vop = redisTemplate.opsForValue();
        AuctionType auctionType = AuctionType.fromText((String) vop.get(AUCTION_TYPE_REDIS+auctionId));
        switch (auctionType) {
            case BASIC:
                return basicAuctionService.eventAuction(auctionId, auctionEventRequest);
            case LIVE:
                return liveAuctionService.eventAuction(auctionId, auctionEventRequest);
            case ERROR:
            default:
                return null;
        }
    }
}
