package com.ryulth.auction.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.ryulth.auction.pojo.response.ProductDetailResponse;
import com.ryulth.auction.pojo.response.ProductListResponse;
import com.ryulth.auction.service.product.ProductService;

import io.lettuce.core.RedisClient;
import io.lettuce.core.XAddArgs;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisStreamCommands;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
public class ProductController {
    private static Logger logger = LoggerFactory.getLogger(AuctionController.class);
    @Autowired
    ProductService productService;

    @PostMapping("/product")
    public String enrollProduct(@RequestBody String payload) throws IOException {
        return productService.enrollProduct(payload);
    }

    @GetMapping("/products")
    public ResponseEntity<ProductListResponse> getAllProducts() throws JsonProcessingException {
        return productService.getAllProducts();
    }

    @GetMapping("/products/{productId}")
    public ResponseEntity<ProductDetailResponse> getOneProducts(
            @PathVariable("productId") Long productId) throws JsonProcessingException {
        return productService.getOneProducts(productId);
    }

    @PutMapping("/products/{productId}")
    public String updateProduct(
            @PathVariable("productId") Long productId,
            @RequestBody String payload) throws IOException {
        return productService.updateProduct(productId, payload);
    }

    @DeleteMapping("/products/{productId}")
    public String deleteProduct(
            @PathVariable("productId") Long productId){
        return productService.deleteProduct(productId);
    }

}
