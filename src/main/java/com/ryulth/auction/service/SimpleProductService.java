package com.ryulth.auction.service;

import org.springframework.stereotype.Component;

@Component
public class SimpleProductService implements ProductService{
    @Override
    public void enrollProduct() {

    }

    @Override
    public String getAllProducts() {
        return "ALL PRODUCT";
    }
}
