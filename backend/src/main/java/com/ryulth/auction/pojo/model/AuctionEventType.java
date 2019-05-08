package com.ryulth.auction.pojo.model;

import java.util.Arrays;

public enum AuctionEventType {
    ENROLL("enroll"),
    START("start"), //등록후 바로 시작하지 않는 상황이 존재 하기 떄문에 생성
    CLOSE("close"),
    BID("bid"), // 입찰
    OUTBID("outbid"), // 즉시 구매
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
