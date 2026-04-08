package com.hanyahunya.stockbasket.global.exception;

import lombok.Getter;

@Getter
public class ExternalException extends RuntimeException {

    private final ErrorCode errorCode;
    private final String detailMessage;

    public ExternalException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
        this.detailMessage = errorCode.getMessage();
    }

    public ExternalException(ErrorCode errorCode, String detailMessage) {
        super(detailMessage);
        this.errorCode = errorCode;
        this.detailMessage = detailMessage;
    }

    public ExternalException(ErrorCode errorCode, Throwable cause) {
        super(errorCode.getMessage(), cause);
        this.errorCode = errorCode;
        this.detailMessage = errorCode.getMessage();
    }
}