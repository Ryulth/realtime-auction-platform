package com.ryulth.auction.pojo.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@Builder
@RequiredArgsConstructor // Jackson will deserialize using this and then invoking setter
@AllArgsConstructor(onConstructor = @__(@JsonIgnore)) // Lombok builder use this
public class AuctionEvent {
    private long version;
    private AuctionEventType auctionEventType;
    private long price;
}
