package com.hanyahunya.stockbasket.global.auth;

import org.springframework.stereotype.Service;

@Service
public class EmailVerificationServiceImpl implements EmailVerificationService{
    @Override
    public void sendVerificationCode(String email) {

    }

    @Override
    public void verifyCode(String email, String code) {

    }
}
