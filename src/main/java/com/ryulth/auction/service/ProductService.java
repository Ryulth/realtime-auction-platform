package com.ryulth.auction.service;

import org.springframework.stereotype.Service;

@Service
public interface ProductService {
    void enrollProduct();
    String getAllProducts();
}
