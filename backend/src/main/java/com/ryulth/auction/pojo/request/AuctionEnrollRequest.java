package com.ryulth.auction.pojo.request;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.ryulth.auction.domain.Product;
import com.ryulth.auction.pojo.model.AuctionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@Builder
@RequiredArgsConstructor // Jackson will deserialize using this and then invoking setter
@AllArgsConstructor(onConstructor = @__(@JsonIgnore)) // Lombok builder use this
public class AuctionEnrollRequest {
    private String auctionType;
    private Long productId;
    @JsonIgnore
    public AuctionType getAuctionTypeEnum(){
        return AuctionType.fromText(this.auctionType);
    }
}
