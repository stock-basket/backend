package com.hanyahunya.stockbasket.infra.scheduler;
import com.hanyahunya.stockbasket.domain.alert.service.AlertService;
import com.hanyahunya.stockbasket.domain.analysis.service.AnalysisService;
import com.hanyahunya.stockbasket.infra.crawler.CrawlerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CrawlScheduler {

    private final CrawlerService crawlerService;
    private final AnalysisService analysisService;
    private final AlertService alertService;

    // 평일 9시~17시, 매 2시간마다 뉴스 크롤링
    @Scheduled(cron = "0 0 9,11,13,15,17 * * MON-FRI")
    public void scheduledCrawl() {
        log.info("[Scheduler] 뉴스 크롤링 시작");
        crawlerService.crawlAll();
        analysisService.analyzeAllPending();
    }

    // 평일 장중(9시~15시30분), 매 5분마다 급등락 감지
    @Scheduled(cron = "0 */5 9-15 * * MON-FRI")
    public void scheduledPriceDetect() {
        alertService.detectAndCreatePriceAlerts();
    }
}
