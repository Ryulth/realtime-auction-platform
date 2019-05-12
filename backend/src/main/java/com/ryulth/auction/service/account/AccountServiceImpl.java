package com.ryulth.auction.service.account;

import com.ryulth.auction.pojo.request.NaverSignUpRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;

@Component
public class AccountServiceImpl implements AccountService {
    @Autowired
    JwtService jwtService;
    @Autowired
    RedisTemplate redisTemplate;

    private static final String ACCOUNT_TOKEN_REDIS = "ryulth:auction:account:";

    @Override
    public String signUp(NaverSignUpRequest naverSignUpRequest) {
        ValueOperations vop = redisTemplate.opsForValue();
        String token = (String) vop.get(ACCOUNT_TOKEN_REDIS+naverSignUpRequest.getEmail());
        if(token == null){
            token = jwtService.create("user", naverSignUpRequest, "user");
            vop.set(ACCOUNT_TOKEN_REDIS+naverSignUpRequest.getEmail(),token);
        }
        return token;
    }
}
