package com.hanyahunya.stockbasket.infra.kiwoom;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class KiwoomReferencePriceScheduler {

    private final PriceStore priceStore;
    private final KiwoomWebSocketClient kiwoomWebSocketClient;

    @Scheduled(cron = "0 0 9 * * MON-FRI", zone = "Asia/Seoul")
    public void snapshotAtMarketOpen() {
        log.info("[Kiwoom] 장 시작 기준가 스냅샷");
        priceStore.snapshotCurrentAsReference();
        kiwoomWebSocketClient.connect();
    }

    @Scheduled(cron = "0 0 10-15 * * MON-FRI", zone = "Asia/Seoul")
    public void hourlyReferenceRefresh() {
        log.info("[Kiwoom] 기준가 갱신 (매 시간 롤링)");
        priceStore.snapshotCurrentAsReference();
    }

    @Scheduled(cron = "0 20 15 * * MON-FRI", zone = "Asia/Seoul")
    public void disconnectAtMarketClose() {
        log.info("[Kiwoom] 장 종료 웹소켓 연결 해제");
        kiwoomWebSocketClient.disconnect();
    }
}
