package com.ryulth.auction.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.ryulth.auction.pojo.response.ProductDetailResponse;
import com.ryulth.auction.pojo.response.ProductListResponse;
import com.ryulth.auction.service.product.ProductService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.RedisSystemException;
import org.springframework.data.redis.connection.stream.ObjectRecord;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.connection.stream.StreamRecords;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StreamOperations;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
public class ProductController {
    private static Logger logger = LoggerFactory.getLogger(AuctionController.class);
    @Autowired
    ProductService productService;
    private static final HttpHeaders httpHeaders = new HttpHeaders();

    @CrossOrigin("*")
    @PostMapping("/product")
    public ResponseEntity<ProductDetailResponse> enrollProduct(@RequestBody String payload) throws IOException {
        httpHeaders.setContentType(MediaType.APPLICATION_JSON_UTF8);
        return new ResponseEntity<>(productService.enrollProduct(payload), httpHeaders, HttpStatus.OK);
    }

    @CrossOrigin("*")
    @GetMapping("/products")
    public ResponseEntity<ProductListResponse> getAllProducts() throws JsonProcessingException {
        return productService.getAllProducts();
    }

    @CrossOrigin("*")
    @GetMapping("/products/{productId}")
    public ResponseEntity<ProductDetailResponse> getOneProducts(
            @PathVariable("productId") Long productId) throws JsonProcessingException {
        return productService.getOneProducts(productId);
    }

    @CrossOrigin("*")
    @PutMapping("/products/{productId}")
    public String updateProduct(
            @PathVariable("productId") Long productId,
            @RequestBody String payload) throws IOException {
        return productService.updateProduct(productId, payload);
    }

    @CrossOrigin("*")
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
        StreamOperations sop = redisTemplate.opsForStream();

        List<ObjectRecord<String, String>> records2 = sop
                .read(String.class, StreamOffset.fromStart("streamTest"));
        String s2 = records2.get(records2.size()-1).getValue();
        System.out.println(s2);
        try {
            xAdd(id + "-" + temp,temp);
        } catch (RedisSystemException e) {
            System.out.println("버전충돌쓰");
            temp+=1;
            redisTest();
        }

        List<ObjectRecord<String, String>> records = sop
                .read(String.class, StreamOffset.fromStart("streamTest"));

        System.out.println(records);
        String s = records.get(records.size()-1).getValue();
        System.out.println(s);
    }
    private boolean xAdd(String id,long version) throws RedisSystemException {
        StreamOperations sop = redisTemplate.opsForStream();
        ObjectRecord<String, String > record = StreamRecords.newRecord()
                .in("streamTest")
                .withId(id)
                .ofObject("test");
//                .ofObject(AuctionEvent.builder().price(1000).version(version).build());
        sop.add(record);
        System.out.println("성공");
        return true;


    }
}
