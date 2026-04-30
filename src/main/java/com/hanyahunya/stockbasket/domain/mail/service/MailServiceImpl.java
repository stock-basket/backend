package com.hanyahunya.stockbasket.domain.mail.service;

import com.hanyahunya.stockbasket.domain.mail.MailErrorCode;
import com.hanyahunya.stockbasket.global.exception.ExternalException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class MailServiceImpl implements MailService {

    private final MailClient mailClient;
    private final TemplateEngine templateEngine;

    @Override
    public void sendSimple(List<String> to, String subject, String content) {
        mailClient.send(to, subject, content);
    }

    @Override
    public void sendTemplate(List<String> to, String subject, String templateName, Map<String, Object> variables) {
        String content = renderTemplate(templateName, variables);
        mailClient.send(to, subject, content);
    }

    private String renderTemplate(String templateName, Map<String, Object> variables) {
        try {
            Context context = new Context();
            context.setVariables(variables);
            return templateEngine.process(templateName, context);
        } catch (Exception e) {
            throw new ExternalException(MailErrorCode.MAIL_TEMPLATE_RENDER_FAILED, e);
        }
    }
}
