package com.hanyahunya.stockbasket.domain.user.service;

import com.hanyahunya.stockbasket.domain.mail.service.MailService;
import com.hanyahunya.stockbasket.domain.user.UserErrorCode;
import com.hanyahunya.stockbasket.domain.user.repository.UserRepository;
import com.hanyahunya.stockbasket.global.auth.JwtProvider;
import com.hanyahunya.stockbasket.global.auth.TokenErrorCode;
import com.hanyahunya.stockbasket.global.exception.BusinessException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailVerificationServiceImplTest {

    @Mock UserRepository userRepository;
    @Mock MailService mailService;
    @Mock JwtProvider jwtProvider;

    @InjectMocks EmailVerificationServiceImpl verificationService;

    // ── sendVerificationCode ───────────────────────────────────────────────────

    @Test
    void sendVerificationCode_정상_메일_발송() {
        String email = "user@example.com";
        when(userRepository.existsByEmail(email)).thenReturn(false);

        assertThatNoException().isThrownBy(() -> verificationService.sendVerificationCode(email));

        verify(mailService).sendTemplate(
                eq(List.of(email)),
                anyString(),
                eq("auth/verification"),
                anyMap()
        );
    }

    @Test
    void sendVerificationCode_코드_6자리() {
        String email = "user@example.com";
        when(userRepository.existsByEmail(email)).thenReturn(false);

        verificationService.sendVerificationCode(email);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Map<String, Object>> varsCaptor = ArgumentCaptor.forClass(Map.class);
        verify(mailService).sendTemplate(any(), any(), any(), varsCaptor.capture());

        String code = (String) varsCaptor.getValue().get("code");
        assertThat(code).hasSize(6).matches("\\d{6}");
    }

    @Test
    void sendVerificationCode_이미_가입된_이메일_EMAIL_ALREADY_EXISTS_예외() {
        String email = "exists@example.com";
        when(userRepository.existsByEmail(email)).thenReturn(true);

        assertThatThrownBy(() -> verificationService.sendVerificationCode(email))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode())
                        .isEqualTo(UserErrorCode.EMAIL_ALREADY_EXISTS));

        verify(mailService, never()).sendTemplate(any(), any(), any(), any());
    }

    // ── verifyCode ─────────────────────────────────────────────────────────────

    @Test
    void verifyCode_정상_검증_후_verified_token_반환() {
        String email = "user@example.com";
        when(userRepository.existsByEmail(email)).thenReturn(false);
        when(jwtProvider.createVerifiedToken(email)).thenReturn("verified-token");

        verificationService.sendVerificationCode(email);

        // 코드 캡처
        @SuppressWarnings("unchecked")
        ArgumentCaptor<Map<String, Object>> varsCaptor = ArgumentCaptor.forClass(Map.class);
        verify(mailService).sendTemplate(any(), any(), any(), varsCaptor.capture());
        String code = (String) varsCaptor.getValue().get("code");

        String token = verificationService.verifyCode(email, code);

        assertThat(token).isEqualTo("verified-token");
    }

    @Test
    void verifyCode_코드_불일치시_INVALID_VERIFICATION_CODE_예외() {
        String email = "user@example.com";
        when(userRepository.existsByEmail(email)).thenReturn(false);
        verificationService.sendVerificationCode(email);

        assertThatThrownBy(() -> verificationService.verifyCode(email, "000000"))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode())
                        .isEqualTo(TokenErrorCode.INVALID_VERIFICATION_CODE));
    }

    @Test
    void verifyCode_코드_전송_안한_이메일_INVALID_VERIFICATION_CODE_예외() {
        assertThatThrownBy(() -> verificationService.verifyCode("noemail@example.com", "123456"))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode())
                        .isEqualTo(TokenErrorCode.INVALID_VERIFICATION_CODE));
    }

    @Test
    void verifyCode_성공후_재사용_방지() {
        String email = "user@example.com";
        when(userRepository.existsByEmail(email)).thenReturn(false);
        when(jwtProvider.createVerifiedToken(email)).thenReturn("verified-token");

        verificationService.sendVerificationCode(email);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Map<String, Object>> varsCaptor = ArgumentCaptor.forClass(Map.class);
        verify(mailService).sendTemplate(any(), any(), any(), varsCaptor.capture());
        String code = (String) varsCaptor.getValue().get("code");

        verificationService.verifyCode(email, code);

        // 두 번째 시도는 실패해야 함 (store 에서 제거됨)
        assertThatThrownBy(() -> verificationService.verifyCode(email, code))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode())
                        .isEqualTo(TokenErrorCode.INVALID_VERIFICATION_CODE));
    }
}
