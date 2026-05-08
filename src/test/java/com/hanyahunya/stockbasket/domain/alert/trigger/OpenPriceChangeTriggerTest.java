package com.hanyahunya.stockbasket.domain.alert.trigger;

import com.hanyahunya.stockbasket.domain.alert.entity.AlertType;
import com.hanyahunya.stockbasket.domain.user.entity.UserSetting;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

class OpenPriceChangeTriggerTest {

    private static final String STOCK_CODE = "005930";

    private OpenPriceChangeTrigger trigger;
    private UserSetting baseSetting;

    @BeforeEach
    void setUp() {
        trigger = new OpenPriceChangeTrigger();
        baseSetting = UserSetting.builder()
                .isGlobalAlertEnabled(true)
                .isVolatilityAlertEnabled(true)
                .volatilityThresholdPercent(5.0)
                .build();
    }

    @Test
    void 임계값_초과_상승시_PRICE_SPIKE_반환() {
        // 100 → 106 = +6% (threshold 5%)
        Optional<TriggerResult> result = trigger.evaluate(STOCK_CODE, 106L, 100L, baseSetting);

        assertThat(result).isPresent();
        assertThat(result.get().alertType()).isEqualTo(AlertType.PRICE_SPIKE);
        assertThat(result.get().changeRate()).isCloseTo(6.0, within(0.001));
    }

    @Test
    void 임계값_초과_하락시_PRICE_DROP_반환() {
        // 100 → 94 = -6%
        Optional<TriggerResult> result = trigger.evaluate(STOCK_CODE, 94L, 100L, baseSetting);

        assertThat(result).isPresent();
        assertThat(result.get().alertType()).isEqualTo(AlertType.PRICE_DROP);
        assertThat(result.get().changeRate()).isCloseTo(-6.0, within(0.001));
    }

    @Test
    void 임계값_미만_변동은_empty() {
        // 100 → 103 = +3% (threshold 5%)
        Optional<TriggerResult> result = trigger.evaluate(STOCK_CODE, 103L, 100L, baseSetting);

        assertThat(result).isEmpty();
    }

    @Test
    void 정확히_임계값과_같으면_PRICE_SPIKE() {
        // 100 → 105 = exactly +5% (>= threshold)
        Optional<TriggerResult> result = trigger.evaluate(STOCK_CODE, 105L, 100L, baseSetting);

        assertThat(result).isPresent();
        assertThat(result.get().alertType()).isEqualTo(AlertType.PRICE_SPIKE);
    }

    @Test
    void 정확히_음수_임계값과_같으면_PRICE_DROP() {
        // 100 → 95 = exactly -5% (<= -threshold)
        Optional<TriggerResult> result = trigger.evaluate(STOCK_CODE, 95L, 100L, baseSetting);

        assertThat(result).isPresent();
        assertThat(result.get().alertType()).isEqualTo(AlertType.PRICE_DROP);
    }

    @Test
    void 글로벌알림_비활성화시_empty() {
        UserSetting disabled = UserSetting.builder()
                .isGlobalAlertEnabled(false)
                .isVolatilityAlertEnabled(true)
                .volatilityThresholdPercent(5.0)
                .build();

        Optional<TriggerResult> result = trigger.evaluate(STOCK_CODE, 106L, 100L, disabled);

        assertThat(result).isEmpty();
    }

    @Test
    void 변동성알림_비활성화시_empty() {
        UserSetting disabled = UserSetting.builder()
                .isGlobalAlertEnabled(true)
                .isVolatilityAlertEnabled(false)
                .volatilityThresholdPercent(5.0)
                .build();

        Optional<TriggerResult> result = trigger.evaluate(STOCK_CODE, 106L, 100L, disabled);

        assertThat(result).isEmpty();
    }

    @Test
    void 글로벌_변동성_모두_비활성화시_empty() {
        UserSetting disabled = UserSetting.builder()
                .isGlobalAlertEnabled(false)
                .isVolatilityAlertEnabled(false)
                .volatilityThresholdPercent(5.0)
                .build();

        Optional<TriggerResult> result = trigger.evaluate(STOCK_CODE, 106L, 100L, disabled);

        assertThat(result).isEmpty();
    }

    @Test
    void 변동률_계산_정확도_검증() {
        // 10000 → 10500 = 5%
        Optional<TriggerResult> result = trigger.evaluate(STOCK_CODE, 10500L, 10000L, baseSetting);

        assertThat(result).isPresent();
        assertThat(result.get().changeRate()).isCloseTo(5.0, within(0.0001));
    }

    @Test
    void 가격_변동없을때_empty() {
        // 100 → 100 = 0%
        Optional<TriggerResult> result = trigger.evaluate(STOCK_CODE, 100L, 100L, baseSetting);

        assertThat(result).isEmpty();
    }
}
