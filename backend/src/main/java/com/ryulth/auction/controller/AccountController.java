package com.ryulth.auction.controller;

import com.ryulth.auction.pojo.request.NaverSignUpRequest;
import com.ryulth.auction.service.account.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class AccountController {
    @Autowired
    AccountService accountService;

    @PostMapping("/signin")
    public String signIn(@RequestBody NaverSignUpRequest naverSignUpRequest) {
        return accountService.signIn(naverSignUpRequest);
    }

    @PostMapping("/accountTest")
    public String test(@RequestHeader("Authorization") String token) {
        return accountService.checkValidation(token);
    }
}
