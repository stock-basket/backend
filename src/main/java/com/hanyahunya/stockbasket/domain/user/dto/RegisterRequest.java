package com.hanyahunya.stockbasket.domain.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequest(

        @NotBlank
        @Pattern(
                regexp = "^[A-Za-z\\d@$!%*#?&]{8,20}$",
                message = "비밀번호는 8~20자이며 영문 대소문자·숫자·특수문자(@$!%*#?&)만 사용 가능합니다."
        )
        String password,

        @NotBlank
        @Size(min = 2, max = 20, message = "닉네임은 2~20자 사이여야 합니다.")
        String nickname,

        /** 뉴스레터 및 마케팅 정보 수신 동의 */
        boolean newsletterAlert,

        /** 급등락 감지 시 이메일 알림 수신 동의 */
        boolean volatilityAlert

) {}
