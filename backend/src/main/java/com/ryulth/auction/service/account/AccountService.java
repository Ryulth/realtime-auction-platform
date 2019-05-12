package com.ryulth.auction.service.account;

import com.ryulth.auction.pojo.request.NaverSignUpRequest;
import org.springframework.stereotype.Service;

@Service
public interface AccountService {
    String signIn(NaverSignUpRequest naverSignUpRequest);
    String checkValidation(String token);
}
