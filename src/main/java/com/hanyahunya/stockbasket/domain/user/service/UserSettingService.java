package com.hanyahunya.stockbasket.domain.user.service;

import com.hanyahunya.stockbasket.domain.user.dto.AlertSettingsUpdateRequest;
import com.hanyahunya.stockbasket.domain.user.dto.AccountInfoUpdateRequest;
import com.hanyahunya.stockbasket.domain.user.dto.PasswordUpdateRequest;

public interface UserSettingService {

    // 알림 설정 저장
    void updateAlertSettings(AlertSettingsUpdateRequest request);

    // 계정 정보 저장
    void updateAccountInfo(AccountInfoUpdateRequest request);

    // 비밀번호 변경
    void updatePassword(PasswordUpdateRequest request);

    // 계정 탈퇴
//    void deleteAccount();

}