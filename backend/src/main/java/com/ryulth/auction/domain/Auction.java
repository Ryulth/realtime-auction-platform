package com.ryulth.auction.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.*;
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
    private Long productId;

    @Column
    private Long price;

    @Column
    private Long version;

    @Column
    private String auctionType;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private ZonedDateTime startTime;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private ZonedDateTime endTime;
}
