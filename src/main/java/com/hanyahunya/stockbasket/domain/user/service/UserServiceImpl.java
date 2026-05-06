package com.hanyahunya.stockbasket.domain.user.service;

import com.hanyahunya.stockbasket.domain.user.UserErrorCode;
import com.hanyahunya.stockbasket.domain.user.dto.*;
import com.hanyahunya.stockbasket.domain.user.entity.Role;
import com.hanyahunya.stockbasket.domain.user.entity.User;
import com.hanyahunya.stockbasket.domain.user.entity.UserSetting;
import com.hanyahunya.stockbasket.domain.user.repository.UserRepository;
import com.hanyahunya.stockbasket.domain.user.repository.UserSettingRepository;
import com.hanyahunya.stockbasket.global.auth.JwtProvider;
import com.hanyahunya.stockbasket.global.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserSettingRepository userSettingRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;

    @Override
    @Transactional
    public void register(String verifiedToken, RegisterRequest request) {
        String email = jwtProvider.parseVerifiedToken(verifiedToken);

        if (userRepository.existsByEmail(email)) {
            throw new BusinessException(UserErrorCode.EMAIL_ALREADY_EXISTS);
        }

        User user = User.builder()
                .id(UUID.randomUUID())
                .email(email)
                .password(passwordEncoder.encode(request.password()))
                .nickname(request.nickname())
                .role(Role.ROLE_USER)
                .build();
        userRepository.save(user);

        userSettingRepository.save(buildUserSetting(user, request));
    }

    private UserSetting buildUserSetting(User user, RegisterRequest request) {
        boolean newsletter  = request.newsletterAlert();
        boolean volatility  = request.volatilityAlert();
        boolean anyAlert    = newsletter || volatility;

        return UserSetting.builder()
                .user(user)
                .isGlobalAlertEnabled(anyAlert)
                .isEmailAlertEnabled(anyAlert)
                .isGoodNewsAlertEnabled(newsletter)
                .isBadNewsAlertEnabled(newsletter)
                .newsImpactThreshold(newsletter ? 70 : 0)
                .isVolatilityAlertEnabled(volatility)
                .volatilityThresholdPercent(volatility ? 3.0 : 0.0)
                .build();
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
                .orElseThrow(() -> new BusinessException(UserErrorCode.USER_NOT_FOUND));
        return new UserResponse(user.getId(), user.getEmail(), user.getNickname());
    }

    @Override
    public void deleteAccount(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(UserErrorCode.USER_NOT_FOUND));
        userRepository.delete(user);
    }
}
