package com.hanyahunya.stockbasket.global.auth;

import com.hanyahunya.stockbasket.global.exception.ErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum TokenErrorCode implements ErrorCode {
    INVALID_TOKEN("유효하지 않은 토큰입니다."),
    EXPIRED_TOKEN("만료된 토큰입니다.");

    private final String code;
    private final HttpStatus status;
    private final String message;

    TokenErrorCode(String message) {
        this.code = "TOKEN_401";
        this.status = HttpStatus.UNAUTHORIZED;
        this.message = message;
    }
}
