package com.hanyahunya.stockbasket.domain.user.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.UUID;

public record PasswordUpdateRequest(
        @JsonIgnore UUID userId,
        String currentPassword,
        String newPassword
) {
}
