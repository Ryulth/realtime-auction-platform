package com.ryulth.auction.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ryulth.auction.domain.Product;
import com.ryulth.auction.pojo.ProductDataRequest;
import com.ryulth.auction.pojo.ProductDataResponse;
import com.ryulth.auction.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Component
public class SimpleProductService implements ProductService {
    private static final String timePattern = "yyyy-MM-dd HH:mm:ss";
    private static final DateTimeFormatter formatter =DateTimeFormatter.ofPattern(timePattern).withZone(ZoneOffset.of("+09:00"));//ZoneId.of("Asia/Seoul"));
    @Autowired
    ObjectMapper objectMapper;
    @Autowired
    ProductRepository productRepository;
    @Override
    public void enrollProduct(String payload) throws IOException {
        System.out.println(payload);

        ProductDataRequest productDataRequest = objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .readValue(payload, ProductDataRequest.class);
        Product newProduct = Product.builder()
                .name(productDataRequest.getName())
                .spec(productDataRequest.getSpec())
                .lowerLimit(productDataRequest.getLowerLimit())
                .upperLimit(productDataRequest.getUpperLimit())
                .startTime(ZonedDateTime.parse(productDataRequest.getStartTime(),formatter))
                .endTime(ZonedDateTime.parse(productDataRequest.getEndTime(),formatter))
                .build();
        productRepository.save(newProduct);
        System.out.println("SAVE PRODUCT");
    }

    @Override
    public String getAllProducts() throws JsonProcessingException {
        List<Product> products = productRepository.findAll();
        ProductDataResponse productDataResponse = ProductDataResponse.builder()
                .products(products)
                .build();
        return objectMapper.writeValueAsString(productDataResponse);
    }

    @Override
    public String getOneProducts(Long productId) throws JsonProcessingException {
        Product product = productRepository.getOne(productId);
        System.out.println(product.getName());
        List<Product> products = new ArrayList<>();
        products.add(product);
        ProductDataResponse productDataResponse = ProductDataResponse.builder()
                .products(products)
                .build();
        System.out.println(productDataResponse.getProducts().get(0));
        return objectMapper.writeValueAsString(product);
    }
}
