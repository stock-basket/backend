package com.hanyahunya.stockbasket.domain.alert;

import com.hanyahunya.stockbasket.global.exception.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum AlertErrorCode implements ErrorCode {
    ;

    private final String code;
    private final HttpStatus status;
    private final String message;
}
