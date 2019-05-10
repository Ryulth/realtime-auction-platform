package com.ryulth.auction.pojo.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.ryulth.auction.pojo.model.AuctionEvent;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.Deque;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@Builder
@RequiredArgsConstructor // Jackson will deserialize using this and then invoking setter
@AllArgsConstructor(onConstructor = @__(@JsonIgnore)) // Lombok builder use this
public class AuctionEventsResponse {
    private Long serverVersion;
    private Deque<AuctionEvent> auctionEvents;
}
