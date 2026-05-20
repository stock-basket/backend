package com.hanyahunya.stockbasket.domain.mail.service;

import com.hanyahunya.stockbasket.domain.mail.MailErrorCode;
import com.hanyahunya.stockbasket.global.exception.ExternalException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MailServiceImplTest {

    @Mock MailClient mailClient;
    @Mock TemplateEngine templateEngine;

    @InjectMocks MailServiceImpl mailService;

    // ── sendSimple ─────────────────────────────────────────────────────────────

    @Test
    void sendSimple_mailClient_위임() {
        List<String> to = List.of("user@example.com");

        mailService.sendSimple(to, "제목", "<p>내용</p>");

        verify(mailClient).send(to, "제목", "<p>내용</p>");
    }

    @Test
    void sendSimple_여러_수신자() {
        List<String> to = List.of("a@example.com", "b@example.com");

        mailService.sendSimple(to, "공지", "내용");

        verify(mailClient).send(to, "공지", "내용");
    }

    // ── sendTemplate ───────────────────────────────────────────────────────────

    @Test
    void sendTemplate_렌더링_후_mailClient_위임() {
        List<String> to = List.of("user@example.com");
        Map<String, Object> vars = Map.of("code", "123456");
        when(templateEngine.process(eq("auth/verification"), any(Context.class)))
                .thenReturn("<html>123456</html>");

        mailService.sendTemplate(to, "인증", "auth/verification", vars);

        verify(mailClient).send(to, "인증", "<html>123456</html>");
    }

    @Test
    void sendTemplate_렌더링_실패시_ExternalException_발생() {
        when(templateEngine.process(anyString(), any(Context.class)))
                .thenThrow(new RuntimeException("template error"));

        assertThatThrownBy(() ->
                mailService.sendTemplate(
                        List.of("user@example.com"), "제목", "invalid/template", Map.of()))
                .isInstanceOf(ExternalException.class)
                .satisfies(ex -> assertThat(((ExternalException) ex).getErrorCode())
                        .isEqualTo(MailErrorCode.MAIL_TEMPLATE_RENDER_FAILED));

        verify(mailClient, never()).send(any(), any(), any());
    }

    @Test
    void sendTemplate_빈_변수맵_정상_처리() {
        List<String> to = List.of("user@example.com");
        when(templateEngine.process(eq("some/template"), any(Context.class)))
                .thenReturn("<html></html>");

        assertThatNoException().isThrownBy(() ->
                mailService.sendTemplate(to, "제목", "some/template", Map.of()));

        verify(mailClient).send(to, "제목", "<html></html>");
    }
}
