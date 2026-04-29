package com.hanyahunya.stockbasket.domain.user.dto;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequest(

        @Email(message = "유효한 이메일 형식이 아닙니다.")
        @NotBlank
        String email,

        @NotBlank
        @Pattern(
                regexp = "^[A-Za-z\\d@$!%*#?&]{8,20}$",
                message = "비밀번호는 8~20자이며 영문 대소문자·숫자·특수문자(@$!%*#?&)만 사용 가능합니다."
        )
        String password,

        @NotBlank
        @Size(min = 2, max = 20, message = "닉네임은 2~20자 사이여야 합니다.")
        String nickname

) {}
