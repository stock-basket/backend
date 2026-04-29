package com.hanyahunya.stockbasket.domain.alert.dto;
import com.hanyahunya.stockbasket.domain.alert.entity.AlertType;
import java.time.LocalDateTime;

public record AlertResponse(
        Long          id,
        String        stockName,
        AlertType alertType,         // PRICE_SPIKE | PRICE_DROP | HIGH_IMPACT_NEWS
        String        message,
        Double        priceChangeRate,   // 급등락 감지 시 변동률 (null 가능)
        boolean       isRead,
        LocalDateTime createdAt
) {}
