package com.hanyahunya.stockbasket.domain.alert;

import com.hanyahunya.stockbasket.domain.alert.service.AlertService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AlertScheduler {

    private final AlertService alertService;

    @Scheduled(cron = "0/30 * 9-14 * * MON-FRI", zone = "Asia/Seoul")
    @Scheduled(cron = "0/30 0-20 15 * * MON-FRI", zone = "Asia/Seoul")
    public void runPriceAlertDetection() {
        alertService.detectAndCreatePriceAlerts();
    }
}
