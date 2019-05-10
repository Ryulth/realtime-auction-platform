package com.ryulth.auction.repository;

import com.ryulth.auction.domain.Auction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AuctionRepository extends JpaRepository<Auction,Long> {
    Optional<Auction> findByProductId(Long productId);
}
