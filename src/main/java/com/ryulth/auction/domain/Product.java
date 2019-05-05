package com.ryulth.auction.domain;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.*;
import java.time.ZonedDateTime;

@Entity
@Data
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
    private Long version;

    @Column
    private Long UpperLimit;

    @Column
    private Long LowerLimit;

    @Column
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private ZonedDateTime startTime;

    @Column
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private ZonedDateTime endTime;
}
