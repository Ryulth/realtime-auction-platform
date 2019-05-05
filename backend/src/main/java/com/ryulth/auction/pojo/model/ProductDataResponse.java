package com.ryulth.auction.pojo.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.ryulth.auction.domain.Product;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@Builder
@RequiredArgsConstructor // Jackson will deserialize using this and then invoking setter
@AllArgsConstructor() // Lombok builder use this
public class ProductDataResponse {
    List<Product> products;
}
