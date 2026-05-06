package com.hanyahunya.stockbasket.domain.alert.trigger;

import com.hanyahunya.stockbasket.domain.alert.entity.AlertType;
import com.hanyahunya.stockbasket.domain.user.entity.UserSetting;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * 시가(기준가) 대비 등락률이 사용자 임계값을 넘으면 PRICE_SPIKE / PRICE_DROP 을 반환한다.
 * referencePrice 는 PriceStore 가 9시 스냅샷(시가)으로 관리한다.
 */
@Component
public class OpenPriceChangeTrigger implements PriceTrigger {

    @Override
    public Optional<TriggerResult> evaluate(String stockCode, long currentPrice, long referencePrice, UserSetting setting) {
        if (!setting.isGlobalAlertEnabled() || !setting.isVolatilityAlertEnabled()) {
            return Optional.empty();
        }

        double changeRate = (double) (currentPrice - referencePrice) / referencePrice * 100.0;
        double threshold = setting.getVolatilityThresholdPercent();

        if (changeRate >= threshold)  return Optional.of(new TriggerResult(AlertType.PRICE_SPIKE, changeRate));
        if (changeRate <= -threshold) return Optional.of(new TriggerResult(AlertType.PRICE_DROP, changeRate));
        return Optional.empty();
    }
}
