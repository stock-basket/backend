package com.hanyahunya.stockbasket.api;

import com.hanyahunya.stockbasket.domain.user.dto.EmailSendRequest;
import com.hanyahunya.stockbasket.domain.user.dto.EmailVerifyRequest;
import com.hanyahunya.stockbasket.global.auth.EmailVerificationService;
import com.hanyahunya.stockbasket.global.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 회원가입 이메일 인증 엔드포인트 (인증 불필요 — whitelist).
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final EmailVerificationService emailVerificationService;

    /**
     * 이메일 인증 코드 발송.
     *
     * <pre>POST /api/auth/email/send</pre>
     * <p>이미 가입된 이메일이면 409 반환.
     */
    @PostMapping("/email/send")
    public ResponseEntity<ApiResponse<Void>> sendVerificationCode(
            @RequestBody @Valid EmailSendRequest request
    ) {
        emailVerificationService.sendVerificationCode(request.email());
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    /**
     * 이메일 인증 코드 검증.
     *
     * <pre>POST /api/auth/email/verify</pre>
     * <p>코드 불일치 또는 만료 시 401 반환.
     */
    @PostMapping("/email/verify")
    public ResponseEntity<ApiResponse<Void>> verifyCode(
            @RequestBody @Valid EmailVerifyRequest request
    ) {
        emailVerificationService.verifyCode(request.email(), request.code());
        return ResponseEntity.ok(ApiResponse.ok(null));
    }
}
