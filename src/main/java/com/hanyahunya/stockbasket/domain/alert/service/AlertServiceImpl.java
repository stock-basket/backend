package com.hanyahunya.stockbasket.domain.alert.service;

import com.hanyahunya.stockbasket.domain.alert.dto.AlertResponse;
import com.hanyahunya.stockbasket.domain.alert.entity.Alert;
import com.hanyahunya.stockbasket.domain.alert.entity.AlertType;
import com.hanyahunya.stockbasket.domain.alert.repository.AlertRepository;
import com.hanyahunya.stockbasket.domain.alert.trigger.PriceTrigger;
import com.hanyahunya.stockbasket.domain.alert.trigger.TriggerResult;
import com.hanyahunya.stockbasket.domain.stock.entity.Stock;
import com.hanyahunya.stockbasket.domain.stock.repository.StockRepository;
import com.hanyahunya.stockbasket.domain.user.entity.User;
import com.hanyahunya.stockbasket.domain.user.entity.UserSetting;
import com.hanyahunya.stockbasket.domain.user.repository.UserRepository;
import com.hanyahunya.stockbasket.domain.user.repository.UserSettingRepository;
import com.hanyahunya.stockbasket.infra.kiwoom.PriceStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.ByteBuffer;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AlertServiceImpl implements AlertService {

    private static final Duration ALERT_COOLDOWN = Duration.ofMinutes(30);

    private final AlertRepository alertRepository;
    private final StockRepository stockRepository;
    private final UserRepository userRepository;
    private final UserSettingRepository userSettingRepository;
    private final PriceStore priceStore;
    private final List<PriceTrigger> triggers;

    @Override
    public List<AlertResponse> getMyAlerts(UUID userId) {
        return alertRepository.findAllByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public List<AlertResponse> getUnreadAlerts(UUID userId) {
        return alertRepository.findAllByUserIdAndReadIsFalseOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public int getUnreadCount(UUID userId) {
        return alertRepository.countByUserIdAndReadIsFalse(userId);
    }

    @Override
    @Transactional
    public void markAsRead(Long alertId) {
        alertRepository.findById(alertId).ifPresent(Alert::markRead);
    }

    @Override
    @Transactional
    public void markAllAsRead(UUID userId) {
        alertRepository.findAllByUserIdAndReadIsFalseOrderByCreatedAtDesc(userId)
                .forEach(Alert::markRead);
    }

    @Override
    @Transactional
    public void detectAndCreatePriceAlerts() {
        List<String> trackedCodes = List.copyOf(priceStore.getTrackedStockCodes());

        for (String stockCode : trackedCodes) {
            Optional<Long> currentOpt = priceStore.getCurrentPrice(stockCode);
            Optional<Long> referenceOpt = priceStore.getReferencePrice(stockCode);

            if (currentOpt.isEmpty() || referenceOpt.isEmpty()) continue;
            long current = currentOpt.get();
            long reference = referenceOpt.get();
            if (reference == 0 || current == 0) continue;

            List<UUID> userIds = toUUIDs(stockRepository.findUserIdsByStockCode(stockCode));
            if (userIds.isEmpty()) continue;

            for (UUID userId : userIds) {
                UserSetting setting = userSettingRepository.findByUser_Id(userId);
                if (setting == null) continue;

                Stock stock = null;
                User user = null;

                for (PriceTrigger trigger : triggers) {
                    Optional<TriggerResult> resultOpt = trigger.evaluate(stockCode, current, reference, setting);
                    if (resultOpt.isEmpty()) continue;

                    TriggerResult result = resultOpt.get();
                    if (priceStore.isCooldownActive(userId, stockCode, result.alertType(), ALERT_COOLDOWN)) continue;

                    if (stock == null) stock = stockRepository.findById(stockCode).orElse(null);
                    if (user == null) user = userRepository.findById(userId).orElse(null);
                    if (stock == null || user == null) break;

                    alertRepository.save(Alert.builder()
                            .user(user)
                            .stock(stock)
                            .alertType(result.alertType())
                            .message(buildMessage(stock.getName(), result))
                            .priceChangeRate(result.changeRate())
                            .build());

                    priceStore.recordAlert(userId, stockCode, result.alertType());
                    log.info("[Alert] {} {} {}% userId={}", stockCode, result.alertType(),
                            String.format("%.2f", result.changeRate()), userId);
                }
            }
        }
    }

    private String buildMessage(String stockName, TriggerResult result) {
        return switch (result.alertType()) {
            case PRICE_SPIKE -> "%s 주가가 %.2f%% 급등했습니다.".formatted(stockName, result.changeRate());
            case PRICE_DROP  -> "%s 주가가 %.2f%% 급락했습니다.".formatted(stockName, Math.abs(result.changeRate()));
            default          -> "%s 알림이 발생했습니다.".formatted(stockName);
        };
    }

    private List<UUID> toUUIDs(List<byte[]> rawList) {
        return rawList.stream()
                .map(bytes -> {
                    ByteBuffer bb = ByteBuffer.wrap(bytes);
                    return new UUID(bb.getLong(), bb.getLong());
                })
                .toList();
    }

    private AlertResponse toResponse(Alert alert) {
        return new AlertResponse(
                alert.getId(),
                alert.getStock().getName(),
                alert.getAlertType(),
                alert.getMessage(),
                alert.getPriceChangeRate(),
                alert.isRead(),
                alert.getCreatedAt()
        );
    }
}
