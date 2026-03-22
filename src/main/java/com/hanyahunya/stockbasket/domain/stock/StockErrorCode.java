package com.hanyahunya.stockbasket.domain.stock;

import com.hanyahunya.stockbasket.global.exception.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum StockErrorCode implements ErrorCode {
    STOCK_NOT_FOUND("STOCK_404", HttpStatus.NOT_FOUND, "존재하지 않는 종목입니다."),
    STOCK_ALREADY_IN_BASKET("STOCK_409", HttpStatus.CONFLICT, "이미 바구니에 담긴 종목입니다."),
    BASKET_LIMIT_EXCEEDED("STOCK_400", HttpStatus.BAD_REQUEST, "바구니 종목 한도를 초과했습니다.");

    private final String code;
    private final HttpStatus status;
    private final String message;
}
