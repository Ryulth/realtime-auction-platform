package com.ryulth.auction.pojo.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@Builder
@RequiredArgsConstructor // Jackson will deserialize using this and then invoking setter
@AllArgsConstructor() // Lombok builder use this
public class ProductDataRequest {
    private String name;
    private String spec;
    private Long upperLimit;
    private Long lowerLimit;
    private String startTime;
    private String endTime;
}
