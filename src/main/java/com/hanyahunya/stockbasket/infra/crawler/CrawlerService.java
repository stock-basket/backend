package com.hanyahunya.stockbasket.infra.crawler;

public interface CrawlerService {
    void crawlByTicker(String ticker);
    void crawlAll();
}
