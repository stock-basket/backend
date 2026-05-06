package com.hanyahunya.stockbasket.domain.user.service;

import com.hanyahunya.stockbasket.domain.mail.service.MailService;
import com.hanyahunya.stockbasket.domain.user.UserErrorCode;
import com.hanyahunya.stockbasket.domain.user.repository.UserRepository;
import com.hanyahunya.stockbasket.global.auth.JwtProvider;
import com.hanyahunya.stockbasket.global.auth.TokenErrorCode;
import com.hanyahunya.stockbasket.global.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
class EmailVerificationServiceImpl implements EmailVerificationService {

    private static final int    CODE_LENGTH  = 6;
    private static final int    TTL_MINUTES  = 5;
    private static final String MAIL_SUBJECT = "[StockBasket] 이메일 인증 코드";
    private static final String TEMPLATE     = "auth/verification";

    private final UserRepository userRepository;
    private final MailService    mailService;
    private final JwtProvider jwtProvider;
    private final SecureRandom   random = new SecureRandom();

    // email → (code, expiry)
    private final Map<String, VerificationEntry> store = new ConcurrentHashMap<>();

    // ─────────────────────────────────────────────────────────────────────────

    @Override
    public void sendVerificationCode(String email) {
        if (userRepository.existsByEmail(email)) {
            throw new BusinessException(UserErrorCode.EMAIL_ALREADY_EXISTS);
        }

        String code = generateCode();
        store.put(email, new VerificationEntry(code, LocalDateTime.now().plusMinutes(TTL_MINUTES)));

        mailService.sendTemplate(
                List.of(email),
                MAIL_SUBJECT,
                TEMPLATE,
                Map.of(
                        "code", code,
                        "expireMinutes", TTL_MINUTES
                )
        );
    }

    @Override
    public String verifyCode(String email, String code) {
        VerificationEntry entry = store.get(email);

        if (entry == null || entry.isExpired() || !entry.code().equals(code)) {
            throw new BusinessException(TokenErrorCode.INVALID_VERIFICATION_CODE);
        }

        store.remove(email); // 검증 완료 후 즉시 삭제 (재사용 방지)
        return jwtProvider.createVerifiedToken(email);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 내부 유틸
    // ─────────────────────────────────────────────────────────────────────────

    private String generateCode() {
        int bound = (int) Math.pow(10, CODE_LENGTH);
        return String.format("%0" + CODE_LENGTH + "d", random.nextInt(bound));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 내부 레코드
    // ─────────────────────────────────────────────────────────────────────────

    private record VerificationEntry(String code, LocalDateTime expiry) {
        boolean isExpired() {
            return LocalDateTime.now().isAfter(expiry);
        }
    }
}