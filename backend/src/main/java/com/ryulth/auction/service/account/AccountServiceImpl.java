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
    public String signIn(NaverSignUpRequest naverSignUpRequest) {
        ValueOperations vop = redisTemplate.opsForValue();
        String token = jwtService.create("user", naverSignUpRequest, "user");
        vop.set(ACCOUNT_TOKEN_REDIS + token, naverSignUpRequest.getEmail());
        return token;
    }

    @Override
    public String checkValidation(String token) {
        ValueOperations vop = redisTemplate.opsForValue();
        String email = (String) vop.get(ACCOUNT_TOKEN_REDIS + token);
        if (email == null) {
            return null;
        }
        return email;
    }


}
