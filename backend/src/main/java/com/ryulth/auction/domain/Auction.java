package com.ryulth.auction.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.*;
import java.time.ZoneId;
import java.time.ZonedDateTime;

@Entity
@Data
@Builder
@AllArgsConstructor() // Lombok builder use this
@Table(name = "auction")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Auction {
    protected Auction(){}
    @Id
    @GeneratedValue
    private Long id;

    @Column
    private Long userId;

    @Column
    private Long productId;

    @Column
    private Long price;

    @Column
    private Long version;

    @Column(columnDefinition = "TINYINT(1)")
    private int onAuction;

    @Column
    private String auctionType;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm")
    private ZonedDateTime createTime;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm")
    private ZonedDateTime startTime;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm")
    private ZonedDateTime endTime;
    @PrePersist
    void setUp(){
        this.onAuction = 1 ;
        ZoneId zoneId = ZoneId.of("Asia/Seoul");
        this.createTime = ZonedDateTime.now(zoneId);
    }
}
