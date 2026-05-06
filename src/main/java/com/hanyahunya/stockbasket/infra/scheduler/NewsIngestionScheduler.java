package com.hanyahunya.stockbasket.infra.scheduler;

import com.hanyahunya.stockbasket.domain.alert.entity.AlertType;
import com.hanyahunya.stockbasket.domain.alert.repository.AlertRepository;
import com.hanyahunya.stockbasket.infra.news.NewsIngestionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class NewsIngestionScheduler {

    private final NewsIngestionService newsIngestionService;
    private final AlertRepository alertRepository;

    // 장전: 8시 50분 (개장 10분 전) — 전날 장후부터 쌓인 뉴스
    @Scheduled(cron = "0 50 8 * * MON-FRI", zone = "Asia/Seoul")
    public void preMarketIngest() {
        log.info("[NewsScheduler] 장전 뉴스 수집 시작");
        newsIngestionService.ingestAll(20);
    }

    // 장중: 12시 00분 (점심)
    @Scheduled(cron = "0 0 12 * * MON-FRI", zone = "Asia/Seoul")
    public void midDayIngest() {
        log.info("[NewsScheduler] 점심 뉴스 수집 시작");
        newsIngestionService.ingestAll(10);
    }

    // 장후: 15시 35분 (장 종료 5분 후)
    @Scheduled(cron = "0 35 15 * * MON-FRI", zone = "Asia/Seoul")
    public void postMarketIngest() {
        log.info("[NewsScheduler] 장후 뉴스 수집 시작");
        newsIngestionService.ingestAll(10);
    }

    // 급등락 감지: 실시간 주가 추적 방식으로 전환 예정 — 스케줄러 방식 보류
//    @Scheduled(cron = "0 */5 9-15 * * MON-FRI", zone = "Asia/Seoul")
//    public void priceAlertTriggeredIngest() {
//        LocalDateTime cutoff = LocalDateTime.now().minusMinutes(10);
//        List<String> stockCodes = alertRepository
//                .findByAlertTypeInAndCreatedAtAfter(
//                        List.of(AlertType.PRICE_SPIKE, AlertType.PRICE_DROP), cutoff)
//                .stream()
//                .map(a -> a.getStock().getStockCode())
//                .distinct()
//                .toList();
//
//        if (stockCodes.isEmpty()) return;
//
//        log.info("[NewsScheduler] 급등락 감지 — {} 종목 뉴스 수집", stockCodes.size());
//        stockCodes.forEach(code -> newsIngestionService.ingestByStock(code, 10));
//    }
}
