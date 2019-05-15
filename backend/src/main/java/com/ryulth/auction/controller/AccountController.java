package com.ryulth.auction.controller;

import com.ryulth.auction.pojo.request.NaverSignInRequest;
import com.ryulth.auction.pojo.response.AccountSignInResponse;
import com.ryulth.auction.service.account.AccountService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
public class AccountController {
    private static Logger logger = LoggerFactory.getLogger(AccountController.class);
    @Autowired
    AccountService accountService;
    private static final HttpHeaders httpHeaders = new HttpHeaders();

    @CrossOrigin("*")
    @PostMapping("/signin")
    public ResponseEntity<AccountSignInResponse> signIn(@RequestBody NaverSignInRequest naverSignUpRequest) {
        logger.info("로그인");
        httpHeaders.setContentType(MediaType.APPLICATION_JSON_UTF8);
        return new ResponseEntity<>(accountService.signIn(naverSignUpRequest), httpHeaders, HttpStatus.OK);
    }

    @CrossOrigin("*")
    @PostMapping("/accountTest")
    public String test(@RequestHeader("Authorization") String token) {
        System.out.println(accountService.getUser(token));
        return accountService.checkValidation(token);
    }
}
