package com.hanyahunya.stockbasket.global.exception;

import com.hanyahunya.stockbasket.domain.user.UserErrorCode;
import com.hanyahunya.stockbasket.domain.mail.MailErrorCode;
import com.hanyahunya.stockbasket.global.response.ErrorResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.*;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
    }

    // ── BusinessException ──────────────────────────────────────────────────────

    @Test
    void handleBusinessException_올바른_상태코드_반환() {
        BusinessException ex = new BusinessException(UserErrorCode.USER_NOT_FOUND);

        ResponseEntity<ErrorResponse> response = handler.handleBusinessException(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void handleBusinessException_올바른_에러코드_반환() {
        BusinessException ex = new BusinessException(UserErrorCode.EMAIL_ALREADY_EXISTS);

        ResponseEntity<ErrorResponse> response = handler.handleBusinessException(ex);

        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCode()).isEqualTo("USER_409");
    }

    @Test
    void handleBusinessException_응답_메시지_포함() {
        BusinessException ex = new BusinessException(UserErrorCode.INVALID_PASSWORD);

        ResponseEntity<ErrorResponse> response = handler.handleBusinessException(ex);

        assertThat(response.getBody().getMessage()).isEqualTo(UserErrorCode.INVALID_PASSWORD.getMessage());
    }

    // ── ExternalException ──────────────────────────────────────────────────────

    @Test
    void handleExternal_500_상태코드_반환() {
        ExternalException ex = new ExternalException(MailErrorCode.MAIL_SEND_FAILED);

        ResponseEntity<ErrorResponse> response = handler.handleExternal(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    void handleExternal_올바른_에러코드_반환() {
        ExternalException ex = new ExternalException(MailErrorCode.MAIL_TEMPLATE_RENDER_FAILED);

        ResponseEntity<ErrorResponse> response = handler.handleExternal(ex);

        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCode()).isEqualTo("MAIL_500_1");
    }

    // ── 일반 예외 ──────────────────────────────────────────────────────────────

    @Test
    void handleException_500_상태코드_반환() {
        Exception ex = new RuntimeException("unexpected error");

        ResponseEntity<ErrorResponse> response = handler.handleException(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    void handleException_SERVER_500_코드_반환() {
        Exception ex = new RuntimeException("unexpected error");

        ResponseEntity<ErrorResponse> response = handler.handleException(ex);

        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCode()).isEqualTo("SERVER_500");
    }
}
