package com.ryulth.auction.service.account;

import com.ryulth.auction.pojo.request.NaverSignUpRequest;
import com.ryulth.auction.pojo.response.AccountSignInResponse;
import org.springframework.stereotype.Service;

@Service
public interface AccountService {
    AccountSignInResponse signIn(NaverSignUpRequest naverSignUpRequest);
    String checkValidation(String token);
}
