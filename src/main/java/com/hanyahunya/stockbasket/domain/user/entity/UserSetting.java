package com.hanyahunya.stockbasket.domain.user.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "user_settings")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class UserSetting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    /** 전체 알림 ON/OFF */
    @Column(nullable = false)
    @Builder.Default
    private boolean isGlobalAlertEnabled = true;

    /** 변동성(가격) 알림 ON/OFF */
    @Column(nullable = false)
    @Builder.Default
    private boolean isVolatilityAlertEnabled = true;

    /** 가격 변동 알림 임계값 (%) */
    @Column(nullable = false)
    @Builder.Default
    private double volatilityThresholdPercent = 5.0;

    /** 악재 뉴스 알림 ON/OFF */
    @Column(nullable = false)
    @Builder.Default
    private boolean isBadNewsAlertEnabled = true;

    /** 호재 뉴스 알림 ON/OFF */
    @Column(nullable = false)
    @Builder.Default
    private boolean isGoodNewsAlertEnabled = true;

    /** 뉴스 영향도 알림 최소 임계값 (1~10) */
    @Column(nullable = false)
    @Builder.Default
    private int newsImpactThreshold = 5;

    /** 이메일 알림 ON/OFF */
    @Column(nullable = false)
    @Builder.Default
    private boolean isEmailAlertEnabled = false;

    // ───────────────────────────────────────────
    // 업데이트 메서드 (setter 대신 도메인 메서드 사용)
    // ───────────────────────────────────────────

    public void updateAlertSettings(
            boolean isGlobalAlertEnabled,
            boolean isVolatilityAlertEnabled,
            double  volatilityThresholdPercent,
            boolean isBadNewsAlertEnabled,
            boolean isGoodNewsAlertEnabled,
            int     newsImpactThreshold,
            boolean isEmailAlertEnabled
    ) {
        this.isGlobalAlertEnabled        = isGlobalAlertEnabled;
        this.isVolatilityAlertEnabled    = isVolatilityAlertEnabled;
        this.volatilityThresholdPercent  = volatilityThresholdPercent;
        this.isBadNewsAlertEnabled       = isBadNewsAlertEnabled;
        this.isGoodNewsAlertEnabled      = isGoodNewsAlertEnabled;
        this.newsImpactThreshold         = newsImpactThreshold;
        this.isEmailAlertEnabled         = isEmailAlertEnabled;
    }
}
