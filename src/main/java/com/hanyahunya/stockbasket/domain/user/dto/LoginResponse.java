package com.hanyahunya.stockbasket.domain.user.dto;
import lombok.Builder;
import lombok.Getter;

@Getter @Builder
public class LoginResponse {
    private Long userId;
    private String email;
    private String nickname;
    private String plan;
    private String accessToken;
}
