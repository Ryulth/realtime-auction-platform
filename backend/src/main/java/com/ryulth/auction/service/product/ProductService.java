package com.ryulth.auction.service.product;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.ryulth.auction.domain.User;
import com.ryulth.auction.pojo.response.ProductDetailResponse;
import com.ryulth.auction.pojo.response.ProductListResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public interface ProductService {
    ProductDetailResponse enrollProduct(String payload, User user) throws IOException;

    ProductListResponse getAllProducts() throws JsonProcessingException;

    ProductDetailResponse getOneProducts(Long productId) throws JsonProcessingException;

    String updateProduct(Long productId, String payload) throws IOException;

    String deleteProduct(Long productId);

}
