package com.hanyahunya.stockbasket.api;
import com.hanyahunya.stockbasket.domain.alert.dto.AlertResponse;
import com.hanyahunya.stockbasket.domain.alert.service.AlertService;
import com.hanyahunya.stockbasket.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/alerts")
@RequiredArgsConstructor
public class AlertController {
    /**
     * test
     */
    private final AlertService alertService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<AlertResponse>>> getMyAlerts(@AuthenticationPrincipal Long userId) {
        return ResponseEntity.ok(ApiResponse.ok(alertService.getMyAlerts(userId)));
    }

    @GetMapping("/unread")
    public ResponseEntity<ApiResponse<List<AlertResponse>>> getUnreadAlerts(@AuthenticationPrincipal Long userId) {
        return ResponseEntity.ok(ApiResponse.ok(alertService.getUnreadAlerts(userId)));
    }

    @GetMapping("/unread/count")
    public ResponseEntity<ApiResponse<Integer>> getUnreadCount(@AuthenticationPrincipal Long userId) {
        return ResponseEntity.ok(ApiResponse.ok(alertService.getUnreadCount(userId)));
    }

    @PatchMapping("/{alertId}/read")
    public ResponseEntity<ApiResponse<Void>> markAsRead(@PathVariable Long alertId) {
        alertService.markAsRead(alertId);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @PatchMapping("/read-all")
    public ResponseEntity<ApiResponse<Void>> markAllAsRead(@AuthenticationPrincipal Long userId) {
        alertService.markAllAsRead(userId);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }
}
