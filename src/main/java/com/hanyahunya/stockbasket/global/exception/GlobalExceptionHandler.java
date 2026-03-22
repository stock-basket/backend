package com.hanyahunya.stockbasket.global.exception;

import com.hanyahunya.stockbasket.global.response.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 비즈니스 예외 처리
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException e) {

        ErrorCode errorCode = e.getErrorCode();

        log.warn("[BusinessException] code={}, message={}", errorCode.getCode(), e.getMessage());

        return ResponseEntity
                .status(errorCode.getStatus())
                .body(ErrorResponse.of(errorCode));
    }

    // 외부 api 등으로 인한 예외 처리
    @ExceptionHandler(ExternalException.class)
    public ResponseEntity<ErrorResponse> handleExternal(ExternalException e) {

        ErrorCode errorCode = e.getErrorCode();

        log.error("[ExternalException] code={}, message={}", errorCode.getCode(), e.getMessage(), e);

        return ResponseEntity
                .status(errorCode.getStatus())
                .body(ErrorResponse.of(errorCode));
    }

    // @Valid 검증 실패 처리
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException e) {

        String detail = e.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .collect(Collectors.joining(", "));

        log.warn("[ValidationException] {}", detail);

        return ResponseEntity
                .badRequest()
                .body(ErrorResponse.of(
                        "VALID_400",
                        detail
                ));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception e) {

        log.error("[UnhandledException]", e);

        return ResponseEntity
                .internalServerError()
                .body(ErrorResponse.of(
                        "SERVER_500",
                        "서버 내부 오류가 발생했습니다."
                ));
    }
}