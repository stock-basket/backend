package com.hanyahunya.stockbasket.global.auth;

import com.hanyahunya.stockbasket.global.exception.ErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum TokenErrorCode implements ErrorCode {

    INVALID_TOKEN("TOKEN_401", HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰입니다."),
    EXPIRED_TOKEN("TOKEN_401", HttpStatus.UNAUTHORIZED, "만료된 토큰입니다."),
    INVALID_VERIFICATION_CODE("TOKEN_401", HttpStatus.UNAUTHORIZED, "인증 코드가 올바르지 않거나 만료되었습니다.");

    private final String     code;
    private final HttpStatus status;
    private final String     message;

    TokenErrorCode(String code, HttpStatus status, String message) {
        this.code    = code;
        this.status  = status;
        this.message = message;
    }
}
