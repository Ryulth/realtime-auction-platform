package com.ryulth.auction.pojo.model;

import java.util.Arrays;

public enum  AuctionType {
    BASIC("basic"), // 비딩 방식
    LIVE("live"), // 경쟁 방식
    ERROR("error");

    private final String value;

    AuctionType(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return this.name();
    }

    public String getValue() {
        return this.value;
    }

    public static AuctionType fromText(String text) {
        return Arrays.stream(values())
                .filter(bl -> bl.value.equalsIgnoreCase(text))
                .findFirst()
                .orElse(AuctionType.ERROR);
    }
}
