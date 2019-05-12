package com.ryulth.auction.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ryulth.auction.pojo.model.AuctionEvent;
import com.ryulth.auction.pojo.response.ProductDetailResponse;
import com.ryulth.auction.pojo.response.ProductListResponse;
import com.ryulth.auction.service.product.ProductService;
import io.lettuce.core.RedisCommandExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.RedisSystemException;
import org.springframework.data.redis.connection.stream.ObjectRecord;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.connection.stream.StreamRecords;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StreamOperations;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.hash.Jackson2HashMapper;
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
            @PathVariable("productId") Long productId) {
        return productService.deleteProduct(productId);
    }

    @Autowired
    RedisTemplate redisTemplate;
    private long temp = 0;

    @GetMapping("/redisTest")
    public void redisTest() {
        long id = 1;
        try {
            xAdd(id + "-" + temp,temp);
        } catch (RedisSystemException e) {
            System.out.println("버전충돌쓰");
            temp+=1;
            redisTest();
        }
        StreamOperations sop = redisTemplate.opsForStream();
        List<ObjectRecord<String, AuctionEvent>> records = sop
                .read(AuctionEvent.class, StreamOffset.fromStart("streamTest"));

        System.out.println(records);
        AuctionEvent auctionEvent = records.get(records.size()-1).getValue();
        System.out.println(auctionEvent);
    }
    private boolean xAdd(String id,long version) throws RedisSystemException {
        StreamOperations sop = redisTemplate.opsForStream();
        ObjectRecord<String, AuctionEvent> record = StreamRecords.newRecord()
                .in("streamTest")
                .withId(id)
                .ofObject(AuctionEvent.builder().price(1000).version(version).build());
        sop.add(record);
        System.out.println("성공");
        return true;


    }
}
