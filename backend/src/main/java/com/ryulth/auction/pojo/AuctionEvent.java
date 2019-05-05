package com.ryulth.auction.pojo;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@Builder
@RequiredArgsConstructor // Jackson will deserialize using this and then invoking setter
@AllArgsConstructor // Lombok builder use this
public class AuctionEvent {
    private Long version; // uuid
    private AuctionEventType auctionEventType;
    private Long price;
}
