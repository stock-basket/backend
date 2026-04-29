package com.hanyahunya.stockbasket.domain.user.service;

import com.hanyahunya.stockbasket.domain.user.dto.LoginRequest;
import com.hanyahunya.stockbasket.domain.user.dto.LoginResponse;
import com.hanyahunya.stockbasket.domain.user.dto.RegisterRequest;
import com.hanyahunya.stockbasket.domain.user.dto.UserResponse;

import java.util.UUID;

public interface UserService {

    /**
     * 회원가입
     *
     * @throws com.hanyahunya.stockbasket.global.exception.BusinessException
     *         EMAIL_ALREADY_EXISTS(409) — 이미 가입된 이메일
     */
    void register(RegisterRequest request);

    /**
     * 로그인 후 access_token 반환
     *
     * @throws com.hanyahunya.stockbasket.global.exception.BusinessException
     *         INVALID_PASSWORD(401) — 이메일 또는 비밀번호 불일치
     */
    LoginResponse login(LoginRequest request);

    /**
     * 내 정보 조회
     *
     * @throws com.hanyahunya.stockbasket.global.exception.BusinessException
     *         USER_NOT_FOUND(404)
     */
    UserResponse getMyInfo(UUID userId);

    /**
     * 회원 탈퇴
     *
     * @throws com.hanyahunya.stockbasket.global.exception.BusinessException
     *         USER_NOT_FOUND(404)
     */
    void deleteAccount(UUID userId);
}
