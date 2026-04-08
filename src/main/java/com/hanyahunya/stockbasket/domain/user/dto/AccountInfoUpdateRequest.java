package com.hanyahunya.stockbasket.domain.user.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.UUID;

public record AccountInfoUpdateRequest(
        @JsonIgnore UUID userId,
        String nickname,
        String email
) {
}
