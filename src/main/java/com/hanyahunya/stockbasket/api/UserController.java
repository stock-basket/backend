package com.hanyahunya.stockbasket.api;

import com.hanyahunya.stockbasket.domain.user.dto.LoginRequest;
import com.hanyahunya.stockbasket.domain.user.dto.LoginResponse;
import com.hanyahunya.stockbasket.domain.user.dto.RegisterRequest;
import com.hanyahunya.stockbasket.domain.user.dto.UserResponse;
import com.hanyahunya.stockbasket.domain.user.service.UserService;
import com.hanyahunya.stockbasket.global.response.ApiResponse;
import com.hanyahunya.stockbasket.global.security.UserPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * 회원가입.
     *
     * <pre>POST /api/users/register</pre>
     * <ul>
     *   <li>이메일 인증이 선행되어야 함 (프론트엔드 플로우)</li>
     *   <li>성공: 201 Created</li>
     *   <li>실패: 409 — 이미 가입된 이메일</li>
     * </ul>
     */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<Void>> register(
            @RequestBody @Valid RegisterRequest request
    ) {
        userService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(null));
    }

    /**
     * 로그인 — access_token 반환.
     *
     * <pre>POST /api/users/login</pre>
     * <ul>
     *   <li>성공: 200 + {@link LoginResponse}</li>
     *   <li>실패: 401 — 이메일 또는 비밀번호 불일치</li>
     * </ul>
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(
            @RequestBody @Valid LoginRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.ok(userService.login(request)));
    }

    /**
     * 내 정보 조회.
     *
     * <pre>GET /api/users/me</pre>
     * <p>Authorization: Bearer {access_token} 필요.
     */
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> getMyInfo(
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ResponseEntity.ok(ApiResponse.ok(userService.getMyInfo(principal.getUserId())));
    }

    /**
     * 회원 탈퇴.
     *
     * <pre>DELETE /api/users/me</pre>
     * <p>Authorization: Bearer {access_token} 필요.
     */
    @DeleteMapping("/me")
    public ResponseEntity<ApiResponse<Void>> deleteAccount(
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        userService.deleteAccount(principal.getUserId());
        return ResponseEntity.ok(ApiResponse.ok(null));
    }
}
