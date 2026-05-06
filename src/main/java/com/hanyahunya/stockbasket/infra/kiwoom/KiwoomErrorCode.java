package com.hanyahunya.stockbasket.infra.kiwoom;

import com.hanyahunya.stockbasket.global.exception.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum KiwoomErrorCode implements ErrorCode {
    TOKEN_FETCH_FAILED("KIWOOM_500", HttpStatus.INTERNAL_SERVER_ERROR, "키움 액세스 토큰 발급 실패"),
    WS_CONNECT_FAILED("KIWOOM_502", HttpStatus.BAD_GATEWAY, "키움 WebSocket 연결 실패"),
    LOGIN_REJECTED("KIWOOM_401", HttpStatus.UNAUTHORIZED, "키움 LOGIN 거부");

    private final String code;
    private final HttpStatus status;
    private final String message;
}
