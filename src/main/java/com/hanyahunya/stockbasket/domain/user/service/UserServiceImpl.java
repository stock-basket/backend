package com.hanyahunya.stockbasket.domain.user.service;
import com.hanyahunya.stockbasket.domain.user.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    @Override
    public void register(RegisterRequest request) {}

    @Override
    public LoginResponse login(LoginRequest request) { return null; }

    @Override
    public UserResponse getMyInfo(Long userId) { return null; }

    @Override
    public void deleteAccount(Long userId) {}
}
