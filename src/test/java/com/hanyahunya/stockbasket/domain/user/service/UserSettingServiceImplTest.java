package com.hanyahunya.stockbasket.domain.user.service;

import com.hanyahunya.stockbasket.domain.user.UserErrorCode;
import com.hanyahunya.stockbasket.domain.user.dto.AccountInfoUpdateRequest;
import com.hanyahunya.stockbasket.domain.user.dto.AlertSettingsUpdateRequest;
import com.hanyahunya.stockbasket.domain.user.dto.PasswordUpdateRequest;
import com.hanyahunya.stockbasket.domain.user.entity.Role;
import com.hanyahunya.stockbasket.domain.user.entity.User;
import com.hanyahunya.stockbasket.domain.user.entity.UserSetting;
import com.hanyahunya.stockbasket.domain.user.repository.UserRepository;
import com.hanyahunya.stockbasket.domain.user.repository.UserSettingRepository;
import com.hanyahunya.stockbasket.global.exception.BusinessException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserSettingServiceImplTest {

    @Mock UserSettingRepository userSettingRepository;
    @Mock UserRepository userRepository;
    @Mock PasswordEncoder passwordEncoder;

    @InjectMocks UserSettingServiceImpl userSettingService;

    // ── updateAlertSettings ────────────────────────────────────────────────────

    @Test
    void updateAlertSettings_설정_정상_업데이트() {
        UUID userId = UUID.randomUUID();
        UserSetting setting = buildUserSetting(buildUser(userId));
        when(userSettingRepository.findByUser_Id(userId)).thenReturn(setting);

        AlertSettingsUpdateRequest request = new AlertSettingsUpdateRequest(
                userId, false, true, 7.0, false, true, 80, true
        );

        assertThatNoException().isThrownBy(() -> userSettingService.updateAlertSettings(request));

        assertThat(setting.isGlobalAlertEnabled()).isFalse();
        assertThat(setting.isVolatilityAlertEnabled()).isTrue();
        assertThat(setting.getVolatilityThresholdPercent()).isEqualTo(7.0);
        assertThat(setting.isBadNewsAlertEnabled()).isFalse();
        assertThat(setting.isGoodNewsAlertEnabled()).isTrue();
        assertThat(setting.getNewsImpactThreshold()).isEqualTo(80);
        assertThat(setting.isEmailAlertEnabled()).isTrue();
    }

    @Test
    void updateAlertSettings_설정_미존재시_USER_NOT_FOUND_예외() {
        UUID userId = UUID.randomUUID();
        when(userSettingRepository.findByUser_Id(userId)).thenReturn(null);

        AlertSettingsUpdateRequest request = new AlertSettingsUpdateRequest(
                userId, true, true, 5.0, true, true, 70, false
        );

        assertThatThrownBy(() -> userSettingService.updateAlertSettings(request))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode())
                        .isEqualTo(UserErrorCode.USER_NOT_FOUND));
    }

    // ── updateAccountInfo ──────────────────────────────────────────────────────

    @Test
    void updateAccountInfo_닉네임_정상_변경() {
        UUID userId = UUID.randomUUID();
        User user = buildUser(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        userSettingService.updateAccountInfo(new AccountInfoUpdateRequest(userId, "newNick"));

        assertThat(user.getNickname()).isEqualTo("newNick");
    }

    @Test
    void updateAccountInfo_미존재_사용자_USER_NOT_FOUND_예외() {
        UUID userId = UUID.randomUUID();
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                userSettingService.updateAccountInfo(new AccountInfoUpdateRequest(userId, "nick")))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode())
                        .isEqualTo(UserErrorCode.USER_NOT_FOUND));
    }

    // ── updatePassword ─────────────────────────────────────────────────────────

    @Test
    void updatePassword_비밀번호_정상_변경() {
        UUID userId = UUID.randomUUID();
        User user = buildUser(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("currentPass", "encoded-password")).thenReturn(true);
        when(passwordEncoder.encode("newPass123!")).thenReturn("new-encoded");

        userSettingService.updatePassword(new PasswordUpdateRequest(userId, "currentPass", "newPass123!"));

        assertThat(user.getPassword()).isEqualTo("new-encoded");
    }

    @Test
    void updatePassword_현재_비밀번호_불일치_INVALID_PASSWORD_예외() {
        UUID userId = UUID.randomUUID();
        User user = buildUser(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrongPass", "encoded-password")).thenReturn(false);

        assertThatThrownBy(() ->
                userSettingService.updatePassword(new PasswordUpdateRequest(userId, "wrongPass", "newPass")))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode())
                        .isEqualTo(UserErrorCode.INVALID_PASSWORD));

        verify(passwordEncoder, never()).encode(any());
    }

    @Test
    void updatePassword_미존재_사용자_USER_NOT_FOUND_예외() {
        UUID userId = UUID.randomUUID();
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                userSettingService.updatePassword(new PasswordUpdateRequest(userId, "cur", "new")))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode())
                        .isEqualTo(UserErrorCode.USER_NOT_FOUND));
    }

    // ── helpers ────────────────────────────────────────────────────────────────

    private User buildUser(UUID id) {
        return User.builder()
                .id(id)
                .email("test@example.com")
                .password("encoded-password")
                .nickname("tester")
                .role(Role.ROLE_USER)
                .build();
    }

    private UserSetting buildUserSetting(User user) {
        return UserSetting.builder()
                .user(user)
                .isGlobalAlertEnabled(true)
                .isVolatilityAlertEnabled(true)
                .volatilityThresholdPercent(5.0)
                .isBadNewsAlertEnabled(true)
                .isGoodNewsAlertEnabled(true)
                .newsImpactThreshold(70)
                .isEmailAlertEnabled(false)
                .build();
    }
}
