package com.hanyahunya.stockbasket.domain.alert.trigger;

import com.hanyahunya.stockbasket.domain.alert.entity.AlertType;

public record TriggerResult(AlertType alertType, double changeRate) {}
