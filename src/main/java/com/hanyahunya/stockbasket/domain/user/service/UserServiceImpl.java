package com.hanyahunya.stockbasket.domain.user.service;
import com.hanyahunya.stockbasket.domain.user.UserErrorCode;
import com.hanyahunya.stockbasket.domain.user.dto.*;
import com.hanyahunya.stockbasket.global.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    @Override
    public void register(RegisterRequest request) {
        if(false) {
            throw new BusinessException(UserErrorCode.USER_NOT_FOUND);
        }
    }

    @Override
    public LoginResponse login(LoginRequest request) { return null; }

    @Override
    public UserResponse getMyInfo(Long userId) { return null; }

    @Override
    public void deleteAccount(Long userId) {}
}
