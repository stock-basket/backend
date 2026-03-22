package com.hanyahunya.stockbasket.domain.user;

import com.hanyahunya.stockbasket.global.exception.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum UserErrorCode implements ErrorCode {
    USER_NOT_FOUND("USER_404", HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."),
    EMAIL_ALREADY_EXISTS("USER_409", HttpStatus.CONFLICT, "이미 사용 중인 이메일입니다."),
    INVALID_PASSWORD("USER_401", HttpStatus.UNAUTHORIZED, "이메일 혹은 비밀번호가 올바르지 않습니다.");

    private final String code;
    private final HttpStatus status;
    private final String message;
}
