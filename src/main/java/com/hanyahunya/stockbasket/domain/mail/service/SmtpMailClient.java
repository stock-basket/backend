package com.hanyahunya.stockbasket.domain.mail.service;

import com.hanyahunya.stockbasket.domain.mail.MailErrorCode;
import com.hanyahunya.stockbasket.global.exception.ExternalException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
class SmtpMailClient implements MailClient {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String from;

    @Override
    public void send(List<String> to, String subject, String content) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(new InternetAddress(from, "StockBasket", "UTF-8"));

            if (to.size() == 1) {
                helper.setTo(to.getFirst());
            } else {
                helper.setTo(new InternetAddress(from, "StockBasket", "UTF-8")); // 더미
                helper.setBcc(to.toArray(new String[0]));
            }

            helper.setSubject(subject);
            helper.setText(content, true);

            mailSender.send(message);

        } catch (jakarta.mail.internet.AddressException e) {
            throw new ExternalException(MailErrorCode.MAIL_INVALID_ADDRESS, e);
        } catch (Exception e) {
            throw new ExternalException(MailErrorCode.MAIL_SEND_FAILED, e);
        }
    }
}
