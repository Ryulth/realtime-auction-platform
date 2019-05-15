package com.ryulth.auction.repository;

import com.ryulth.auction.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User,Long> {
    Optional<User> findByNaverId(String naverId);
}
