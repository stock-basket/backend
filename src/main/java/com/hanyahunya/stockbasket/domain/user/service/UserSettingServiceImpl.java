package com.hanyahunya.stockbasket.domain.user.service;

import com.hanyahunya.stockbasket.domain.user.UserErrorCode;
import com.hanyahunya.stockbasket.domain.user.dto.AccountInfoUpdateRequest;
import com.hanyahunya.stockbasket.domain.user.dto.AlertSettingsUpdateRequest;
import com.hanyahunya.stockbasket.domain.user.dto.PasswordUpdateRequest;
import com.hanyahunya.stockbasket.domain.user.entity.User;
import com.hanyahunya.stockbasket.domain.user.entity.UserSetting;
import com.hanyahunya.stockbasket.domain.user.repository.UserRepository;
import com.hanyahunya.stockbasket.domain.user.repository.UserSettingRepository;
import com.hanyahunya.stockbasket.global.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserSettingServiceImpl implements UserSettingService{

    private final UserSettingRepository userSettingRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void updateAlertSettings(AlertSettingsUpdateRequest request) {
        UserSetting userSetting = userSettingRepository.findByUser_Id(request.userId());
        if (userSetting == null) {
            throw new BusinessException(UserErrorCode.USER_NOT_FOUND);
        }

        userSetting.updateAlertSettings(
                request.isGlobalAlertEnabled(),
                request.isVolatilityAlertEnabled(),
                request.volatilityThresholdPercent(),
                request.isBadNewsAlertEnabled(),
                request.isGoodNewsAlertEnabled(),
                request.newsImpactThreshold(),
                request.isEmailAlertEnabled()
        );
    }

    @Override
    @Transactional
    public void updateAccountInfo(AccountInfoUpdateRequest request) {
        User user = userRepository.findById(request.userId())
                .orElseThrow(() -> new BusinessException(UserErrorCode.USER_NOT_FOUND));
        if (request.email() != null) {
            user.updateEmail(request.email());
        }

        if(request.nickname() != null) {
            user.updateNickname(request.nickname());
        }
    }

    @Override
    @Transactional
    public void updatePassword(PasswordUpdateRequest request) {
        User user = userRepository.findById(request.userId())
                .orElseThrow(() -> new BusinessException(UserErrorCode.USER_NOT_FOUND));

        if (!passwordEncoder.matches(request.currentPassword(), user.getPassword())) {
            throw new BusinessException(UserErrorCode.INVALID_PASSWORD);
        }

        user.updatePassword(passwordEncoder.encode(request.newPassword()));
    }
}
