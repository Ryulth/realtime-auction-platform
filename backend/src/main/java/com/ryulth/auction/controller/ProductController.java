package com.ryulth.auction.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.ryulth.auction.domain.User;
import com.ryulth.auction.pojo.response.ProductDetailResponse;
import com.ryulth.auction.pojo.response.ProductListResponse;
import com.ryulth.auction.service.account.AccountService;
import com.ryulth.auction.service.product.ProductService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
public class ProductController {
    private static Logger logger = LoggerFactory.getLogger(AuctionController.class);
    @Autowired
    ProductService productService;
    @Autowired
    AccountService accountService;
    private static final HttpHeaders httpHeaders = new HttpHeaders();

    @CrossOrigin("*")
    @PostMapping("/product")
    public ResponseEntity<ProductDetailResponse> enrollProduct(
            @RequestHeader("Authorization") String token,
            @RequestBody String payload) throws IOException {
        User user = accountService.getUser(token);
        httpHeaders.setContentType(MediaType.APPLICATION_JSON_UTF8);
        return new ResponseEntity<>(productService.enrollProduct(payload,user), httpHeaders, HttpStatus.OK);
    }

    @CrossOrigin("*")
    @GetMapping("/products")
    public ResponseEntity<ProductListResponse> getAllProducts() throws JsonProcessingException {
        httpHeaders.setContentType(MediaType.APPLICATION_JSON_UTF8);
        return new ResponseEntity<>(productService.getAllProducts(),httpHeaders,HttpStatus.OK);
    }

    @CrossOrigin("*")
    @GetMapping("/products/{productId}")
    public ResponseEntity<ProductDetailResponse> getOneProducts(
            @PathVariable("productId") Long productId) throws JsonProcessingException {
        return new ResponseEntity<>(productService.getOneProducts(productId),httpHeaders,HttpStatus.OK);
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
}
