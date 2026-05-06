package com.hanyahunya.stockbasket.infra.kiwoom;

import com.hanyahunya.stockbasket.domain.alert.entity.AlertType;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class PriceStore {

    private final ConcurrentHashMap<String, Long> currentPrices = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Long> referencePrices = new ConcurrentHashMap<>();
    // key: "userId::stockCode::ALERT_TYPE"
    private final ConcurrentHashMap<String, Instant> lastAlertTimes = new ConcurrentHashMap<>();

    public void updateCurrentPrice(String stockCode, long price) {
        currentPrices.put(stockCode, price);
        // 기준가가 아직 없으면 첫 수신가를 기준으로 설정
        referencePrices.putIfAbsent(stockCode, price);
    }

    public void setReferencePrice(String stockCode, long price) {
        referencePrices.put(stockCode, price);
    }

    /** currentPrices를 referencePrices로 복사 (매 시간 롤링 기준가 갱신) */
    public void snapshotCurrentAsReference() {
        currentPrices.forEach(referencePrices::put);
    }

    public Optional<Long> getCurrentPrice(String stockCode) {
        return Optional.ofNullable(currentPrices.get(stockCode));
    }

    public Optional<Long> getReferencePrice(String stockCode) {
        return Optional.ofNullable(referencePrices.get(stockCode));
    }

    public Set<String> getTrackedStockCodes() {
        return currentPrices.keySet();
    }

    public void initStock(String stockCode) {
        // WebSocket 구독 시 슬롯 생성 — 첫 REAL 메시지가 올 때 실제 값 설정됨
        currentPrices.putIfAbsent(stockCode, 0L);
    }

    public boolean isCooldownActive(UUID userId, String stockCode, AlertType type, Duration cooldown) {
        String key = cooldownKey(userId, stockCode, type);
        Instant last = lastAlertTimes.get(key);
        return last != null && Instant.now().isBefore(last.plus(cooldown));
    }

    public void recordAlert(UUID userId, String stockCode, AlertType type) {
        lastAlertTimes.put(cooldownKey(userId, stockCode, type), Instant.now());
    }

    private String cooldownKey(UUID userId, String stockCode, AlertType type) {
        return userId + "::" + stockCode + "::" + type.name();
    }
}
