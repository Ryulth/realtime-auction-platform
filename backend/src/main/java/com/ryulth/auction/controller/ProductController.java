package com.ryulth.auction.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.ryulth.auction.pojo.response.ProductListResponse;
import com.ryulth.auction.pojo.response.ProductDetailResponse;
import com.ryulth.auction.service.product.ProductService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
public class ProductController {
    private static Logger logger = LoggerFactory.getLogger(AuctionController.class);
    @Autowired
    ProductService productService;

    @PostMapping("/product")
    public String postProduct(@RequestBody String payload) throws IOException {
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
