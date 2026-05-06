package com.hanyahunya.stockbasket.domain.mail.service;

import java.util.List;
import java.util.Map;

/**
 * 메일 발송 서비스 인터페이스.
 *
 * <p>단순 텍스트/HTML 메일과 Thymeleaf 템플릿 메일 두 가지 발송 방식을 제공한다.
 * 실제 전송은 내부적으로 {@link MailClient}에 위임하며,
 * 호출부는 전송 방식(SMTP 등)에 의존하지 않는다.
 *
 * <p>템플릿 파일 위치: {@code resources/templates/} 하위
 * (예: {@code "mail/verification"} → {@code resources/templates/mail/verification.html})
 */
public interface MailService {

    /**
     * 단순 HTML 메일을 발송한다.
     *
     * @param to      수신자 이메일 목록 (1개 이상)
     * @param subject 메일 제목
     * @param content HTML 본문
     * @throws com.hanyahunya.stockbasket.global.exception.ExternalException
     *         전송 실패 시 (MAIL_500)
     */
    void sendSimple(List<String> to, String subject, String content);

    /**
     * Thymeleaf 템플릿을 렌더링하여 메일을 발송한다.
     *
     * @param to           수신자 이메일 목록 (1개 이상)
     * @param subject      메일 제목
     * @param templateName 템플릿 이름 (확장자 제외, 예: {@code "mail/verification"})
     * @param variables    템플릿에 바인딩할 변수 맵
     * @throws com.hanyahunya.stockbasket.global.exception.ExternalException
     *         템플릿 렌더링 실패(MAIL_500_1) 또는 전송 실패(MAIL_500)
     */
    void sendTemplate(List<String> to, String subject, String templateName, Map<String, Object> variables);
}