package com.hanyahunya.stockbasket.api;

import com.hanyahunya.stockbasket.domain.user.dto.AccountInfoUpdateRequest;
import com.hanyahunya.stockbasket.domain.user.dto.AlertSettingsUpdateRequest;
import com.hanyahunya.stockbasket.domain.user.dto.PasswordUpdateRequest;
import com.hanyahunya.stockbasket.domain.user.service.UserSettingService;
import com.hanyahunya.stockbasket.global.response.ApiResponse;
import com.hanyahunya.stockbasket.global.security.UserPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * 계정 설정 / 알림 설정 엔드포인트.
 *
 * <p>모든 요청: Authorization: Bearer {access_token} 필요.
 */
@RestController
@RequestMapping("/api/users/me")
@RequiredArgsConstructor
public class UserSettingController {

    private final UserSettingService userSettingService;

    /**
     * 닉네임·이메일 변경.
     *
     * <pre>PATCH /api/users/me/account</pre>
     * <ul>
     *   <li>성공: 200</li>
     *   <li>실패: 409 — 이미 사용 중인 이메일</li>
     * </ul>
     */
    @PatchMapping("/account")
    public ResponseEntity<ApiResponse<Void>> updateAccountInfo(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestBody @Valid AccountInfoUpdateRequest request
    ) {
        // userId 는 JSON body 에 포함되지 않으므로 새 레코드로 조합하여 서비스에 전달
        userSettingService.updateAccountInfo(
                new AccountInfoUpdateRequest(principal.getUserId(), request.nickname(), request.email())
        );
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    /**
     * 비밀번호 변경.
     *
     * <pre>PATCH /api/users/me/password</pre>
     * <ul>
     *   <li>성공: 200</li>
     *   <li>실패: 401 — 현재 비밀번호 불일치</li>
     * </ul>
     */
    @PatchMapping("/password")
    public ResponseEntity<ApiResponse<Void>> updatePassword(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestBody @Valid PasswordUpdateRequest request
    ) {
        userSettingService.updatePassword(
                new PasswordUpdateRequest(principal.getUserId(), request.currentPassword(), request.newPassword())
        );
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    /**
     * 알림 설정 저장.
     *
     * <pre>PATCH /api/users/me/alert-settings</pre>
     */
    @PatchMapping("/alert-settings")
    public ResponseEntity<ApiResponse<Void>> updateAlertSettings(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestBody AlertSettingsUpdateRequest request
    ) {
        userSettingService.updateAlertSettings(
                new AlertSettingsUpdateRequest(
                        principal.getUserId(),
                        request.isGlobalAlertEnabled(),
                        request.isVolatilityAlertEnabled(),
                        request.volatilityThresholdPercent(),
                        request.isBadNewsAlertEnabled(),
                        request.isGoodNewsAlertEnabled(),
                        request.newsImpactThreshold(),
                        request.isEmailAlertEnabled()
                )
        );
        return ResponseEntity.ok(ApiResponse.ok(null));
    }
}
