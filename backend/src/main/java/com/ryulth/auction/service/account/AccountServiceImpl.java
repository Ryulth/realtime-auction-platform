package com.ryulth.auction.service.account;

import com.ryulth.auction.domain.User;
import com.ryulth.auction.pojo.request.NaverSignInRequest;
import com.ryulth.auction.pojo.response.AccountSignInResponse;
import com.ryulth.auction.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class AccountServiceImpl implements AccountService {
    private static Logger logger = LoggerFactory.getLogger(AccountServiceImpl.class);
    @Autowired
    JwtService jwtService;
    @Autowired
    RedisTemplate redisTemplate;
    @Autowired
    UserRepository userRepository;
    private static final String ACCOUNT_TOKEN_REDIS = "ryulth:auction:account:";

    @Override
    public AccountSignInResponse signIn(NaverSignInRequest naverSignUpRequest) {
        User user = userRepository.findByNaverId(naverSignUpRequest.getNaverId()).orElse(User.builder().naverId(naverSignUpRequest.getNaverId())
                .email(naverSignUpRequest.getEmail())
                .nickName(naverSignUpRequest.getNickName())
                .build());
        if(user.getId() ==null){
            logger.info("회원가입");
            userRepository.save(user);
        }
        String token = jwtService.create("user", user, "auction");
        return AccountSignInResponse.builder().jwtToken(token).build();
    }
    @Override
    public boolean checkValidation(String token) {
        return jwtService.decode(token);
    }

    @Override
    public User getUser(String token) {
        Map<String,Object> user = jwtService.get("user",token);
        return userRepository.getOne(Long.valueOf((Integer) user.get("id")));
    }
}
