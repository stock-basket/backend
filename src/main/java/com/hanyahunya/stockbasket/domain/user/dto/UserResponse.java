package com.hanyahunya.stockbasket.domain.user.dto;
import lombok.Builder;
import lombok.Getter;

@Getter @Builder
public class UserResponse {
    private Long id;
    private String email;
    private String nickname;
    private String plan;
    private int basketCount;
}
