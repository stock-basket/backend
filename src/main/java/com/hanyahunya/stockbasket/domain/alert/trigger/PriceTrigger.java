package com.hanyahunya.stockbasket.domain.alert.trigger;

import com.hanyahunya.stockbasket.domain.user.entity.UserSetting;

import java.util.Optional;

public interface PriceTrigger {
    Optional<TriggerResult> evaluate(String stockCode, long currentPrice, long referencePrice, UserSetting setting);
}
