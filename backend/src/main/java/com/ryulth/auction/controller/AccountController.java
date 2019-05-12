package com.ryulth.auction.controller;

import com.ryulth.auction.pojo.request.NaverSignUpRequest;
import com.ryulth.auction.service.account.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class AccountController {
    @Autowired
    AccountService accountService;
    @PostMapping("/signup")
    public String signUp(@RequestBody NaverSignUpRequest naverSignUpRequest){
        return accountService.signUp(naverSignUpRequest);
    }
}
