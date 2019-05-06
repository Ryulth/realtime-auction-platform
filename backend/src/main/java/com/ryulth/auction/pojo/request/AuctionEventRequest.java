package com.ryulth.auction.pojo.request;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.ryulth.auction.domain.Product;
import com.ryulth.auction.pojo.model.AuctionEventType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@Builder
@RequiredArgsConstructor // Jackson will deserialize using this and then invoking setter
@AllArgsConstructor(onConstructor = @__(@JsonIgnore)) // Lombok builder use this
public class AuctionEventRequest {
    private String auctionEventType;
    private Long price;
    @JsonIgnore
    public AuctionEventType getAuctionEventTypeEnum(){
        return AuctionEventType.fromText(this.auctionEventType);
    }
}
