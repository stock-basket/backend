package com.hanyahunya.stockbasket.api;

import com.hanyahunya.stockbasket.domain.alert.dto.AlertResponse;
import com.hanyahunya.stockbasket.domain.alert.service.AlertService;
import com.hanyahunya.stockbasket.global.response.ApiResponse;
import com.hanyahunya.stockbasket.global.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 알림(Alert) 관련 엔드포인트.
 *
 * <p>모든 요청: Authorization: Bearer {access_token} 필요.
 */
@RestController
@RequestMapping("/api/alerts")
@RequiredArgsConstructor
public class AlertController {

    private final AlertService alertService;

    /**
     * 전체 알림 목록 조회 (최신순).
     *
     * <pre>GET /api/alerts</pre>
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<AlertResponse>>> getMyAlerts(
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ResponseEntity.ok(ApiResponse.ok(alertService.getMyAlerts(principal.getUserId())));
    }

    /**
     * 미읽 알림 목록 조회 (최신순).
     *
     * <pre>GET /api/alerts/unread</pre>
     */
    @GetMapping("/unread")
    public ResponseEntity<ApiResponse<List<AlertResponse>>> getUnreadAlerts(
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ResponseEntity.ok(ApiResponse.ok(alertService.getUnreadAlerts(principal.getUserId())));
    }

    /**
     * 미읽 알림 개수 조회.
     *
     * <pre>GET /api/alerts/unread/count</pre>
     * <p>네비게이션 배지(nav-badge) 갱신에 사용.
     */
    @GetMapping("/unread/count")
    public ResponseEntity<ApiResponse<Integer>> getUnreadCount(
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ResponseEntity.ok(ApiResponse.ok(alertService.getUnreadCount(principal.getUserId())));
    }

    /**
     * 단건 읽음 처리.
     *
     * <pre>PATCH /api/alerts/{alertId}/read</pre>
     * <ul>
     *   <li>실패: 404 — 존재하지 않는 알림</li>
     * </ul>
     */
    @PatchMapping("/{alertId}/read")
    public ResponseEntity<ApiResponse<Void>> markAsRead(
            @PathVariable Long alertId
    ) {
        alertService.markAsRead(alertId);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    /**
     * 전체 읽음 처리.
     *
     * <pre>PATCH /api/alerts/read-all</pre>
     */
    @PatchMapping("/read-all")
    public ResponseEntity<ApiResponse<Void>> markAllAsRead(
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        alertService.markAllAsRead(principal.getUserId());
        return ResponseEntity.ok(ApiResponse.ok(null));
    }
}
