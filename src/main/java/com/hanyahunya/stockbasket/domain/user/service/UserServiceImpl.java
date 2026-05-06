package com.hanyahunya.stockbasket.domain.user.service;
import com.hanyahunya.stockbasket.domain.user.UserErrorCode;
import com.hanyahunya.stockbasket.domain.user.dto.*;
import com.hanyahunya.stockbasket.domain.user.entity.Role;
import com.hanyahunya.stockbasket.domain.user.entity.User;
import com.hanyahunya.stockbasket.domain.user.repository.UserRepository;
import com.hanyahunya.stockbasket.global.auth.JwtProvider;
import com.hanyahunya.stockbasket.global.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;

    @Override
    @Transactional
    public void register(RegisterRequest request) {

        if (userRepository.existsByEmail(request.email())) {
            throw new BusinessException(UserErrorCode.EMAIL_ALREADY_EXISTS);
        }

        String encodedPassword = passwordEncoder.encode(request.password());

        User user = User.builder()
                .id(UUID.randomUUID())
                .email(request.email())
                .password(encodedPassword)
                .nickname(request.nickname())
                .role(Role.ROLE_USER)
                .build();

        userRepository.save(user);
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new BusinessException(UserErrorCode.INVALID_PASSWORD));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new BusinessException(UserErrorCode.INVALID_PASSWORD);
        }

        String token = jwtProvider.createAccessToken(user.getId(), user.getRole());
        return new LoginResponse(token);
    }

    @Override
    public UserResponse getMyInfo(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(()-> new BusinessException(UserErrorCode.USER_NOT_FOUND));
        return new UserResponse(user.getId(), user.getEmail(), user.getNickname());
    }

    @Override
    public void deleteAccount(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(UserErrorCode.USER_NOT_FOUND));
        userRepository.delete(user);
    }
}
