package com.ryulth.auction.controller;

import com.ryulth.auction.pojo.request.NaverSignUpRequest;
import com.ryulth.auction.service.account.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


@RestController
public class AccountController {
    @Autowired
    AccountService accountService;

    @CrossOrigin("*")
    @PostMapping("/signin")
    public String signIn(@RequestBody NaverSignUpRequest naverSignUpRequest) {
        System.out.println("로그인");
        return accountService.signIn(naverSignUpRequest);
    }

    @PostMapping("/accountTest")
    public String test(@RequestHeader("Authorization") String token) {
        return accountService.checkValidation(token);
    }
}
