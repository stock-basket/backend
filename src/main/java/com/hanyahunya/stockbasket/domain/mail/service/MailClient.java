package com.hanyahunya.stockbasket.domain.mail.service;

import java.util.List;

interface MailClient {
    /**
     * HTML 메일을 발송한다.
     *
     * @param to      수신자 이메일 목록 (1개 이상)
     * @param subject 메일 제목
     * @param content HTML 본문
     * @throws com.hanyahunya.stockbasket.global.exception.ExternalException
     *         주소 형식 오류(MAIL_400) 또는 전송 실패(MAIL_500)
     */
    void send(List<String> to, String subject, String content);
}
