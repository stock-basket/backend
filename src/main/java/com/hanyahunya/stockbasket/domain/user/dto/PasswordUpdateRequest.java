package com.hanyahunya.stockbasket.domain.user.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.UUID;

/** 비밀번호 변경 요청 */
public record PasswordUpdateRequest(
        @JsonIgnore UUID   userId,
        String currentPassword,
        String newPassword
) {
}
