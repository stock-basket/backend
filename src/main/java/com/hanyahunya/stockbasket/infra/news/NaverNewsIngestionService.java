package com.hanyahunya.stockbasket.infra.news;

import com.hanyahunya.stockbasket.domain.news.entity.News;
import com.hanyahunya.stockbasket.domain.news.repository.NewsRepository;
import com.hanyahunya.stockbasket.domain.stock.entity.Stock;
import com.hanyahunya.stockbasket.domain.stock.repository.StockRepository;
import com.hanyahunya.stockbasket.infra.news.dto.NaverNewsResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Service
public class NaverNewsIngestionService implements NewsIngestionService {

    private static final int DEFAULT_COUNT = 10;

    private final NaverNewsProperties props;
    private final StockRepository stockRepository;
    private final NewsRepository newsRepository;
    private final RestClient restClient;

    public NaverNewsIngestionService(
            NaverNewsProperties props,
            StockRepository stockRepository,
            NewsRepository newsRepository,
            @Qualifier("naverNewsRestClient") RestClient restClient) {
        this.props = props;
        this.stockRepository = stockRepository;
        this.newsRepository = newsRepository;
        this.restClient = restClient;
    }

    @Override
    public void ingestByStock(String stockCode, int count) {
        Stock stock = stockRepository.findById(stockCode).orElse(null);
        if (stock == null) {
            log.warn("[NewsIngestion] 종목 없음: {}", stockCode);
            return;
        }

        String query = stock.getName();
        int display = Math.min(Math.max(count, 1), 100);

        NaverNewsResponse response = restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .scheme("https")
                        .host("openapi.naver.com")
                        .path("/v1/search/news.json")
                        .queryParam("query", query)
                        .queryParam("display", display)
                        .queryParam("sort", "date")
                        .build())
                .header("X-Naver-Client-Id", props.clientId())
                .header("X-Naver-Client-Secret", props.clientSecret())
                .retrieve()
                .body(NaverNewsResponse.class);

        if (response == null || response.items() == null) {
            log.debug("[NewsIngestion] 결과 없음: {}", stockCode);
            return;
        }

        int saved = 0;
        for (NaverNewsResponse.Item item : response.items()) {
            if (newsRepository.existsBySourceUrl(item.link())) continue;
            try {
                newsRepository.save(News.builder()
                        .stock(stock)
                        .title(stripHtml(item.title()))
                        .content(stripHtml(item.description()))
                        .sourceUrl(item.link())
                        .publishedAt(parsePubDate(item.pubDate()))
                        .build());
                saved++;
            } catch (DataIntegrityViolationException ignored) {}
        }
        log.info("[NewsIngestion] {} 뉴스 {}건 저장", stockCode, saved);
    }

    @Override
    public void ingestByStock(String stockCode) {
        ingestByStock(stockCode, DEFAULT_COUNT);
    }

    @Override
    public void ingestAll(int count) {
        List<Stock> stocks = stockRepository.findAll();
        log.info("[NewsIngestion] 전체 수집 시작 — 종목 {}개", stocks.size());
        stocks.forEach(s -> ingestByStock(s.getStockCode(), count));
    }

    private LocalDateTime parsePubDate(String pubDate) {
        try {
            return ZonedDateTime.parse(pubDate, DateTimeFormatter.RFC_1123_DATE_TIME)
                    .withZoneSameInstant(ZoneOffset.UTC)
                    .toLocalDateTime();
        } catch (Exception e) {
            log.debug("[NewsIngestion] pubDate 파싱 실패: {}", pubDate);
            return LocalDateTime.now();
        }
    }

    private String stripHtml(String html) {
        if (html == null) return null;
        return html.replaceAll("<[^>]*>", "")
                .replace("&quot;", "\"")
                .replace("&amp;", "&")
                .replace("&lt;", "<")
                .replace("&gt;", ">");
    }
}
