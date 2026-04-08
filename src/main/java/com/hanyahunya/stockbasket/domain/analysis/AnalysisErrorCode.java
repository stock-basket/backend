package com.hanyahunya.stockbasket.domain.analysis;

import com.hanyahunya.stockbasket.global.exception.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum AnalysisErrorCode implements ErrorCode {
    ANALYSIS_NOT_FOUND("ANALYSIS_404", HttpStatus.NOT_FOUND, "분석 결과가 존재하지 않습니다.");

    private final String code;
    private final HttpStatus status;
    private final String message;
}
