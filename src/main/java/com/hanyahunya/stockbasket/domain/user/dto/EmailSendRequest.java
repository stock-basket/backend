package com.hanyahunya.stockbasket.domain.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/** 이메일 인증 코드 발송 요청 */
public record EmailSendRequest(
        @Email @NotBlank String email
) {}
