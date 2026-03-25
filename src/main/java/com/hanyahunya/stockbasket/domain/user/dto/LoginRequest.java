package com.hanyahunya.stockbasket.domain.user.dto;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;

public record LoginRequest(
        @Email
        String email,
        @NotBlank
        @Pattern(
                regexp = "^[A-Za-z\\d@$!%*#?&]{8,20}$",
                message = "비밀번호는 8~20자이며, 영문 대소문자, 숫자, 특수문자(@$!%*#?&)만 사용가능합니다."
        )
        String password
) {
}
