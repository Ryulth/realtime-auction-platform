package com.ryulth.auction.pojo.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.ryulth.auction.domain.Product;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.ZonedDateTime;
import java.util.ArrayDeque;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@Builder
@RequiredArgsConstructor // Jackson will deserialize using this and then invoking setter
@AllArgsConstructor(onConstructor = @__(@JsonIgnore)) // Lombok builder use this
public class AuctionEventData {
    private Long auctionId;
    private AuctionType auctionType;
    private Product product;
    ArrayDeque<AuctionEvent> auctionEvents;
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private ZonedDateTime startTime;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private ZonedDateTime endTime;
}
