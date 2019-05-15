package com.ryulth.auction.service.product;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ryulth.auction.domain.Product;
import com.ryulth.auction.pojo.request.ProductDataRequest;
import com.ryulth.auction.pojo.response.ProductDetailResponse;
import com.ryulth.auction.pojo.response.ProductListResponse;
import com.ryulth.auction.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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
    public ProductDetailResponse enrollProduct(String payload) throws IOException {
        ProductDataRequest productDataRequest = objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .readValue(payload, ProductDataRequest.class);
        Product newProduct = Product.builder()
                .name(productDataRequest.getName())
                .spec(productDataRequest.getSpec())
                .lowerLimit(productDataRequest.getLowerLimit())
                .upperLimit(productDataRequest.getUpperLimit())
                .build();
        productRepository.save(newProduct);
        return ProductDetailResponse.builder().product(newProduct).build();
    }

    @Override
    public ResponseEntity<ProductListResponse> getAllProducts() throws JsonProcessingException {
        List<Product> products = productRepository.findAll();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON_UTF8);
        return new ResponseEntity<>(ProductListResponse.builder().products(products).build(),httpHeaders, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<ProductDetailResponse> getOneProducts(Long productId) throws JsonProcessingException {
        Product product = productRepository.getOne(productId);
        httpHeaders.setContentType(MediaType.APPLICATION_JSON_UTF8);
        return new ResponseEntity<>(ProductDetailResponse.builder().product(product).build(),httpHeaders, HttpStatus.OK);
    }

    @Override
    public String  updateProduct(Long productId, String payload) throws IOException {
        ProductDataRequest productDataRequest = objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .readValue(payload, ProductDataRequest.class);
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
