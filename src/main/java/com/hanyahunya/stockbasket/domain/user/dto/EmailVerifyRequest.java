package com.hanyahunya.stockbasket.domain.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** 이메일 인증 코드 검증 요청 */
public record EmailVerifyRequest(
        @Email @NotBlank                          String email,
        @NotBlank @Size(min = 6, max = 6)         String code
) {}