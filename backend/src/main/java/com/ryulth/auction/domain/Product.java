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
@Table(name = "product")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Product {
    protected Product(){}
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

    @Column(columnDefinition = "TINYINT(1)")
    private int onSale;

    @Column
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private ZonedDateTime createTime;

    @PrePersist
    void setUp(){
        this.onSale = 1 ;
        ZoneId seoulZoneId = ZoneId.of("Asia/Seoul");
        this.createTime = ZonedDateTime.now(seoulZoneId);
    }
}
