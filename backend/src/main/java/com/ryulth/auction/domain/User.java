package com.ryulth.auction.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import javax.persistence.*;

@Entity
@Data
@Builder
@AllArgsConstructor() // Lombok builder use this
@Table(name = "user")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class User {
    protected User(){}
    @Id
    @GeneratedValue
    private Long id;

    @Column
    private String email;

    @Column
    private String nickName;
}
