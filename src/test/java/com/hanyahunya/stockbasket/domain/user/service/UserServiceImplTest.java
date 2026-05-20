package com.hanyahunya.stockbasket.domain.user.service;

import com.hanyahunya.stockbasket.domain.user.UserErrorCode;
import com.hanyahunya.stockbasket.domain.user.dto.LoginRequest;
import com.hanyahunya.stockbasket.domain.user.dto.LoginResponse;
import com.hanyahunya.stockbasket.domain.user.dto.RegisterRequest;
import com.hanyahunya.stockbasket.domain.user.dto.UserResponse;
import com.hanyahunya.stockbasket.domain.user.entity.Role;
import com.hanyahunya.stockbasket.domain.user.entity.User;
import com.hanyahunya.stockbasket.domain.user.entity.UserSetting;
import com.hanyahunya.stockbasket.domain.user.repository.UserRepository;
import com.hanyahunya.stockbasket.domain.user.repository.UserSettingRepository;
import com.hanyahunya.stockbasket.global.auth.JwtProvider;
import com.hanyahunya.stockbasket.global.exception.BusinessException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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
class UserServiceImplTest {

    @Mock UserRepository userRepository;
    @Mock UserSettingRepository userSettingRepository;
    @Mock PasswordEncoder passwordEncoder;
    @Mock JwtProvider jwtProvider;

    @InjectMocks UserServiceImpl userService;

    // ── register ───────────────────────────────────────────────────────────────

    @Test
    void register_정상_유저와_설정_저장() {
        String email = "new@example.com";
        when(jwtProvider.parseVerifiedToken("verified-token")).thenReturn(email);
        when(userRepository.existsByEmail(email)).thenReturn(false);
        when(passwordEncoder.encode("Password1!")).thenReturn("encoded");

        userService.register("verified-token", new RegisterRequest("Password1!", "tester", false, false));

        verify(userRepository).save(any(User.class));
        verify(userSettingRepository).save(any(UserSetting.class));
    }

    @Test
    void register_이메일_중복이면_EMAIL_ALREADY_EXISTS_예외() {
        when(jwtProvider.parseVerifiedToken("token")).thenReturn("dup@example.com");
        when(userRepository.existsByEmail("dup@example.com")).thenReturn(true);

        assertThatThrownBy(() ->
                userService.register("token", new RegisterRequest("Pass1!", "nick", false, false)))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode())
                        .isEqualTo(UserErrorCode.EMAIL_ALREADY_EXISTS));

        verify(userRepository, never()).save(any());
    }

    @Test
    void register_뉴스레터만_동의시_뉴스알림_활성화() {
        String email = "test@example.com";
        when(jwtProvider.parseVerifiedToken("token")).thenReturn(email);
        when(userRepository.existsByEmail(email)).thenReturn(false);
        when(passwordEncoder.encode(any())).thenReturn("encoded");

        userService.register("token", new RegisterRequest("Pass1!", "nick", true, false));

        ArgumentCaptor<UserSetting> captor = ArgumentCaptor.forClass(UserSetting.class);
        verify(userSettingRepository).save(captor.capture());
        UserSetting saved = captor.getValue();

        assertThat(saved.isGoodNewsAlertEnabled()).isTrue();
        assertThat(saved.isBadNewsAlertEnabled()).isTrue();
        assertThat(saved.getNewsImpactThreshold()).isEqualTo(70);
        assertThat(saved.isVolatilityAlertEnabled()).isFalse();
    }

    @Test
    void register_변동성만_동의시_변동성알림_활성화() {
        String email = "test@example.com";
        when(jwtProvider.parseVerifiedToken("token")).thenReturn(email);
        when(userRepository.existsByEmail(email)).thenReturn(false);
        when(passwordEncoder.encode(any())).thenReturn("encoded");

        userService.register("token", new RegisterRequest("Pass1!", "nick", false, true));

        ArgumentCaptor<UserSetting> captor = ArgumentCaptor.forClass(UserSetting.class);
        verify(userSettingRepository).save(captor.capture());
        UserSetting saved = captor.getValue();

        assertThat(saved.isVolatilityAlertEnabled()).isTrue();
        assertThat(saved.getVolatilityThresholdPercent()).isEqualTo(3.0);
        assertThat(saved.isGoodNewsAlertEnabled()).isFalse();
    }

    @Test
    void register_모두_비동의시_알림_비활성화() {
        String email = "test@example.com";
        when(jwtProvider.parseVerifiedToken("token")).thenReturn(email);
        when(userRepository.existsByEmail(email)).thenReturn(false);
        when(passwordEncoder.encode(any())).thenReturn("encoded");

        userService.register("token", new RegisterRequest("Pass1!", "nick", false, false));

        ArgumentCaptor<UserSetting> captor = ArgumentCaptor.forClass(UserSetting.class);
        verify(userSettingRepository).save(captor.capture());
        UserSetting saved = captor.getValue();

        assertThat(saved.isGlobalAlertEnabled()).isFalse();
        assertThat(saved.isEmailAlertEnabled()).isFalse();
        assertThat(saved.isVolatilityAlertEnabled()).isFalse();
    }

    // ── login ──────────────────────────────────────────────────────────────────

    @Test
    void login_정상_액세스토큰_반환() {
        UUID userId = UUID.randomUUID();
        User user = buildUser(userId, "test@example.com", "encoded");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("rawPass", "encoded")).thenReturn(true);
        when(jwtProvider.createAccessToken(userId, Role.ROLE_USER)).thenReturn("access-token");

        LoginResponse response = userService.login(new LoginRequest("test@example.com", "rawPass"));

        assertThat(response.accessToken()).isEqualTo("access-token");
    }

    @Test
    void login_이메일_미존재시_INVALID_PASSWORD_예외() {
        when(userRepository.findByEmail(any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.login(new LoginRequest("none@example.com", "pass")))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode())
                        .isEqualTo(UserErrorCode.INVALID_PASSWORD));
    }

    @Test
    void login_비밀번호_불일치시_INVALID_PASSWORD_예외() {
        User user = buildUser(UUID.randomUUID(), "test@example.com", "encoded");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong", "encoded")).thenReturn(false);

        assertThatThrownBy(() -> userService.login(new LoginRequest("test@example.com", "wrong")))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode())
                        .isEqualTo(UserErrorCode.INVALID_PASSWORD));
    }

    // ── getMyInfo ──────────────────────────────────────────────────────────────

    @Test
    void getMyInfo_정상_반환() {
        UUID userId = UUID.randomUUID();
        User user = buildUser(userId, "test@example.com", "encoded");
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        UserResponse response = userService.getMyInfo(userId);

        assertThat(response.id()).isEqualTo(userId);
        assertThat(response.email()).isEqualTo("test@example.com");
        assertThat(response.nickname()).isEqualTo("tester");
    }

    @Test
    void getMyInfo_미존재_사용자_USER_NOT_FOUND_예외() {
        UUID userId = UUID.randomUUID();
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getMyInfo(userId))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode())
                        .isEqualTo(UserErrorCode.USER_NOT_FOUND));
    }

    // ── deleteAccount ──────────────────────────────────────────────────────────

    @Test
    void deleteAccount_정상_삭제() {
        UUID userId = UUID.randomUUID();
        User user = buildUser(userId, "test@example.com", "encoded");
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        assertThatNoException().isThrownBy(() -> userService.deleteAccount(userId));
        verify(userRepository).delete(user);
    }

    @Test
    void deleteAccount_미존재_사용자_USER_NOT_FOUND_예외() {
        UUID userId = UUID.randomUUID();
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.deleteAccount(userId))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode())
                        .isEqualTo(UserErrorCode.USER_NOT_FOUND));

        verify(userRepository, never()).delete(any());
    }

    // ── helper ─────────────────────────────────────────────────────────────────

    private User buildUser(UUID id, String email, String password) {
        return User.builder()
                .id(id)
                .email(email)
                .password(password)
                .nickname("tester")
                .role(Role.ROLE_USER)
                .build();
    }
}
