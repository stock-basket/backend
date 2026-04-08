package com.hanyahunya.stockbasket.domain.user.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.UUID;

public record AlertSettingsUpdateRequest(
        @JsonIgnore UUID userId,
        boolean isGlobalAlertEnabled,      // 전체 알림
        boolean isVolatilityAlertEnabled,  // 급등락 감지 알림
        double volatilityThresholdPercent,    // 감지 임계값 (%)
        boolean isBadNewsAlertEnabled,     // 고영향 악재 뉴스 알림
        boolean isGoodNewsAlertEnabled,    // 고영향 호재 뉴스 알림
        int newsImpactThreshold,           // 알림 최소 영향력 점수
        boolean isEmailAlertEnabled        // 이메일 알림
) {
}
