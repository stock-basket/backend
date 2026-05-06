package com.hanyahunya.stockbasket.infra.news.crawler;

import com.hanyahunya.stockbasket.global.exception.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum CrawlerErrorCode implements ErrorCode {
    PROVIDER_NOT_FOUND("CRA_500", HttpStatus.INTERNAL_SERVER_ERROR, "해당 Provider의 크롤러가 구현되지 않았습니다.");

    private final String code;
    private final HttpStatus status;
    private final String message;
}
