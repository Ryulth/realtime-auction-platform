package com.ryulth.auction.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.*;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

@Entity
@Data
@Builder
@AllArgsConstructor() // Lombok builder use this
@Table(name = "products")
public class Product {
    @Id
    @GeneratedValue
    private Long id;

    @Column
    private String name;

    @Lob
    @Column(nullable = true)
    private String spec;

    @Column
    private Long upperLimit;

    @Column
    private Long lowerLimit;

    @Column
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private ZonedDateTime createTime;

    @Column
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private ZonedDateTime startTime;

    @Column
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private ZonedDateTime endTime;

    @PrePersist
    void setUp(){
        ZoneId seoulZoneId = ZoneId.of("Asia/Seoul");
        ZoneOffset seoulZoneOffset = ZoneOffset.of("+09:00");
        this.createTime = ZonedDateTime.now(seoulZoneOffset);
    }

    protected Product(){}
}
