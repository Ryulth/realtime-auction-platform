package com.ryulth.auction.service.product;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ryulth.auction.domain.Product;
import com.ryulth.auction.domain.User;
import com.ryulth.auction.pojo.request.ProductEnrollRequest;
import com.ryulth.auction.pojo.response.ProductDetailResponse;
import com.ryulth.auction.pojo.response.ProductListResponse;
import com.ryulth.auction.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

@Component
@Primary
public class ProductServiceImpl implements ProductService {
    private static final HttpHeaders httpHeaders = new HttpHeaders();
    @Autowired
    ObjectMapper objectMapper;
    @Autowired
    ProductRepository productRepository;
    @Override
    public ProductDetailResponse enrollProduct(String payload, User user) throws IOException {
        ProductEnrollRequest productEnrollRequest = objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .readValue(payload, ProductEnrollRequest.class);
        Product newProduct = Product.builder()
                .userId(user.getId())
                .name(productEnrollRequest.getName())
                .spec(productEnrollRequest.getSpec())
                .lowerLimit(productEnrollRequest.getLowerLimit())
                .upperLimit(productEnrollRequest.getUpperLimit())
                .build();
        productRepository.save(newProduct);
        return ProductDetailResponse.builder().product(newProduct).build();
    }

    @Override
    public ProductListResponse getAllProducts() throws JsonProcessingException {
        List<Product> products = productRepository.findAll();
        return ProductListResponse.builder().products(products).build();
    }

    @Override
    public ProductDetailResponse getOneProducts(Long productId) throws JsonProcessingException {
        Product product = productRepository.getOne(productId);
        return ProductDetailResponse.builder().product(product).build();
    }

    @Override
    public String  updateProduct(Long productId, String payload) throws IOException {
        ProductEnrollRequest productDataRequest = objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .readValue(payload, ProductEnrollRequest.class);
        Product updateProduct = productRepository.getOne(productId);
        updateProduct.setName(productDataRequest.getName());
        updateProduct.setSpec(productDataRequest.getSpec());
        updateProduct.setLowerLimit(productDataRequest.getLowerLimit());
        updateProduct.setUpperLimit(productDataRequest.getUpperLimit());
        productRepository.save(updateProduct);
        return "UPDATE PRODUCT";
    }

    @Override
    public String deleteProduct(Long productId) {
        Product product = productRepository.getOne(productId);
        product.setOnSale(0);
        productRepository.save(product);
        return "DELETE PRODUCT";
    }
}
