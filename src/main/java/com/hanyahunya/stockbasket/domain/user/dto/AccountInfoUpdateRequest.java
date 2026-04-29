package com.hanyahunya.stockbasket.domain.user.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

import java.util.UUID;

/** 닉네임·이메일 변경 요청 */
public record AccountInfoUpdateRequest(
        @JsonIgnore UUID   userId,      // SecurityContext 에서 주입, 역직렬화 제외
        @Size(min = 2, max = 20) String nickname,
        @Email               String email
) {}