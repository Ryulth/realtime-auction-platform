package com.ryulth.auction.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.ryulth.auction.service.AuctionService;
import com.ryulth.auction.service.ProductService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
public class ProductController {
    private static Logger logger = LoggerFactory.getLogger(AuctionController.class);
    @Autowired
    ProductService productService;

    @GetMapping("/products")
    public String getAllProducts() throws JsonProcessingException {
        return productService.getAllProducts();
    }

    @PostMapping("/product")
    public void postProduct(@RequestBody String reqBody) throws IOException {
        logger.info("POST product",reqBody);
        productService.enrollProduct(reqBody);
    }
}
