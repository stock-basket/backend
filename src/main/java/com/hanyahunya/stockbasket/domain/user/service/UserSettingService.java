package com.hanyahunya.stockbasket.domain.user.service;

import com.hanyahunya.stockbasket.domain.user.dto.AccountInfoUpdateRequest;
import com.hanyahunya.stockbasket.domain.user.dto.AlertSettingsUpdateRequest;
import com.hanyahunya.stockbasket.domain.user.dto.PasswordUpdateRequest;

public interface UserSettingService {

    /**
     * 알림 설정 저장
     *
     * @throws com.hanyahunya.stockbasket.global.exception.BusinessException
     *         USER_NOT_FOUND(404)
     */
    void updateAlertSettings(AlertSettingsUpdateRequest request);

    /**
     * 닉네임·이메일 변경
     *
     * @throws com.hanyahunya.stockbasket.global.exception.BusinessException
     *         USER_NOT_FOUND(404), EMAIL_ALREADY_EXISTS(409)
     */
    void updateAccountInfo(AccountInfoUpdateRequest request);

    /**
     * 비밀번호 변경
     *
     * @throws com.hanyahunya.stockbasket.global.exception.BusinessException
     *         USER_NOT_FOUND(404), INVALID_PASSWORD(401) — 현재 비밀번호 불일치
     */
    void updatePassword(PasswordUpdateRequest request);
}
