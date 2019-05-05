package com.ryulth.auction.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.mysql.cj.protocol.ColumnDefinition;
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
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
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
    private int flag;

    @Column(columnDefinition = "TINYINT(1)")
    private int onAuction;
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
        this.flag = 1 ;
        this.onAuction = 0;
        ZoneId seoulZoneId = ZoneId.of("Asia/Seoul");
        this.createTime = ZonedDateTime.now(seoulZoneId);
    }

    protected Product(){}
}
