package com.ryulth.auction.service.account;

import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public interface JwtService {
    <T> String create(String key, T data, String subject);
    boolean decode(String token);
    Map<String, Object> get(String key, String token);
}
