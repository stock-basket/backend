package com.hanyahunya.stockbasket.domain.user.entity;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

class UserSettingTest {

    private User buildUser() {
        return User.builder()
                .id(UUID.randomUUID())
                .email("test@example.com")
                .password("encoded")
                .nickname("tester")
                .role(Role.ROLE_USER)
                .build();
    }

    @Test
    void builder_기본값_확인() {
        UserSetting setting = UserSetting.builder()
                .user(buildUser())
                .build();

        assertThat(setting.isGlobalAlertEnabled()).isTrue();
        assertThat(setting.isVolatilityAlertEnabled()).isTrue();
        assertThat(setting.getVolatilityThresholdPercent()).isEqualTo(5.0);
        assertThat(setting.isBadNewsAlertEnabled()).isTrue();
        assertThat(setting.isGoodNewsAlertEnabled()).isTrue();
        assertThat(setting.getNewsImpactThreshold()).isEqualTo(5);
        assertThat(setting.isEmailAlertEnabled()).isFalse();
    }

    @Test
    void updateAlertSettings_모든_필드_업데이트() {
        UserSetting setting = UserSetting.builder()
                .user(buildUser())
                .build();

        setting.updateAlertSettings(false, true, 7.5, false, true, 80, true);

        assertThat(setting.isGlobalAlertEnabled()).isFalse();
        assertThat(setting.isVolatilityAlertEnabled()).isTrue();
        assertThat(setting.getVolatilityThresholdPercent()).isEqualTo(7.5);
        assertThat(setting.isBadNewsAlertEnabled()).isFalse();
        assertThat(setting.isGoodNewsAlertEnabled()).isTrue();
        assertThat(setting.getNewsImpactThreshold()).isEqualTo(80);
        assertThat(setting.isEmailAlertEnabled()).isTrue();
    }

    @Test
    void updateAlertSettings_전체알림_비활성화() {
        UserSetting setting = UserSetting.builder()
                .user(buildUser())
                .isGlobalAlertEnabled(true)
                .isEmailAlertEnabled(true)
                .build();

        setting.updateAlertSettings(false, false, 0.0, false, false, 0, false);

        assertThat(setting.isGlobalAlertEnabled()).isFalse();
        assertThat(setting.isVolatilityAlertEnabled()).isFalse();
        assertThat(setting.isEmailAlertEnabled()).isFalse();
    }

    @Test
    void builder_커스텀_값_설정() {
        UserSetting setting = UserSetting.builder()
                .user(buildUser())
                .isGlobalAlertEnabled(false)
                .isVolatilityAlertEnabled(false)
                .volatilityThresholdPercent(3.0)
                .isBadNewsAlertEnabled(false)
                .isGoodNewsAlertEnabled(false)
                .newsImpactThreshold(70)
                .isEmailAlertEnabled(true)
                .build();

        assertThat(setting.isGlobalAlertEnabled()).isFalse();
        assertThat(setting.getVolatilityThresholdPercent()).isEqualTo(3.0);
        assertThat(setting.getNewsImpactThreshold()).isEqualTo(70);
        assertThat(setting.isEmailAlertEnabled()).isTrue();
    }
}
