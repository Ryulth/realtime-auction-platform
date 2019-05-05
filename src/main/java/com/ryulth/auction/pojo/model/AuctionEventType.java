package com.ryulth.auction.pojo.model;

import java.util.Arrays;

public enum AuctionEventType {
    ENROLL("enroll"),
    START("start"),
    CLOSE("close"),
    BIDDING("bidding"), // 입찰
    OUTBIDDING("outbidding"), // 즉시 구매가
    ERROR("error");

    private final String value;

    AuctionEventType(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return this.name();
    }

    public String getValue() {
        return this.value;
    }

    public static AuctionEventType fromText(String text) {
        return Arrays.stream(values())
                .filter(bl -> bl.value.equalsIgnoreCase(text))
                .findFirst()
                .orElse(AuctionEventType.ERROR);
    }
}
