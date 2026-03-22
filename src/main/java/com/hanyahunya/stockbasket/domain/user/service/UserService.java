package com.hanyahunya.stockbasket.domain.user.service;
import com.hanyahunya.stockbasket.domain.user.dto.*;

public interface UserService {
    /**
     * 회원가입 메서드
     * @param request
     */
    void register(RegisterRequest request);
    LoginResponse login(LoginRequest request);
    UserResponse getMyInfo(Long userId);
    void deleteAccount(Long userId);
}
