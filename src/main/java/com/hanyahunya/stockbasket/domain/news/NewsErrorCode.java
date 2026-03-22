package com.hanyahunya.stockbasket.domain.news;

import com.hanyahunya.stockbasket.global.exception.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum NewsErrorCode implements ErrorCode {
    NEWS_NOT_FOUND("NEWS_404", HttpStatus.NOT_FOUND, "존재하지 않는 뉴스입니다.");

    private final String code;
    private final HttpStatus status;
    private final String message;
}
