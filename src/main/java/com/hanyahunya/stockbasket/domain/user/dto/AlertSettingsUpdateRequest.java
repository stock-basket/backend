package com.hanyahunya.stockbasket.domain.user.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.UUID;

/** 알림 설정 변경 요청 */
public record AlertSettingsUpdateRequest(
        @JsonIgnore UUID    userId,
        boolean isGlobalAlertEnabled,
        boolean isVolatilityAlertEnabled,
        double  volatilityThresholdPercent,
        boolean isBadNewsAlertEnabled,
        boolean isGoodNewsAlertEnabled,
        int     newsImpactThreshold,
        boolean isEmailAlertEnabled
) {}
