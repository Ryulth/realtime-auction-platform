package com.ryulth.auction.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public interface ProductService {
    void enrollProduct(String payload) throws IOException;
    String getAllProducts() throws JsonProcessingException;
}
