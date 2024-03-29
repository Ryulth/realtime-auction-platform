package com.ryulth.auction.service.account;

import io.jsonwebtoken.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.UnsupportedEncodingException;
import java.util.Map;

@Component
public class JwtServiceImpl implements JwtService{

    private @Value("${jwt.secret}")
    String SALT;
    @Override
    public <T> String create(String key, T data, String subject){
        String jwt = Jwts.builder()
                .setHeaderParam("typ", "JWT")
                //.setHeaderParam("regDate", System.currentTimeMillis())
                .setSubject(subject)
                .claim(key, data)
                .signWith(SignatureAlgorithm.HS256, this.generateKey())
                .compact();
        return jwt;
    }

    @Override
    public boolean decode(String token) {
        try {
            Jws<Claims> jws = Jwts.parser().setSigningKey(this.generateKey()).parseClaimsJws(token);
            return true;
        }catch (SignatureException e){
            return false;
        }
    }

    private byte[] generateKey(){
        byte[] key = null;
        try {
            key = SALT.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return key;
    }

    @Override
    public Map<String, Object> get(String key,String token) {
        try {
            Jws<Claims> jws = Jwts.parser().setSigningKey(this.generateKey()).parseClaimsJws(token);
            return (Map<String, Object>) jws.getBody().get(key);
        }catch (SignatureException e){
            return null;
        }
    }
}
