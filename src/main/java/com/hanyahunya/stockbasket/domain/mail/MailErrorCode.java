package com.hanyahunya.stockbasket.domain.mail;

import com.hanyahunya.stockbasket.global.exception.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum MailErrorCode implements ErrorCode {
    MAIL_SEND_FAILED("MAIL_500", HttpStatus.INTERNAL_SERVER_ERROR, "메일 전송에 실패했습니다."),
    MAIL_TEMPLATE_RENDER_FAILED("MAIL_500_1", HttpStatus.INTERNAL_SERVER_ERROR, "메일 템플릿 렌더링에 실패했습니다."),
    MAIL_INVALID_ADDRESS("MAIL_400", HttpStatus.BAD_REQUEST, "유효하지 않은 이메일 주소입니다.");

    private final String code;
    private final HttpStatus status;
    private final String message;
}
