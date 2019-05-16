package com.ryulth.auction.service.account;

import com.ryulth.auction.domain.User;
import com.ryulth.auction.pojo.request.NaverSignInRequest;
import com.ryulth.auction.pojo.response.AccountSignInResponse;
import org.springframework.stereotype.Service;

@Service
public interface AccountService {
    AccountSignInResponse signIn(NaverSignInRequest naverSignUpRequest);
    boolean checkValidation(String token);
    User getUser(String token);
}
