package com.hanyahunya.stockbasket.infra.crawler;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NaverNewsCrawler implements CrawlerService {

    @Override
    public void crawlByTicker(String ticker) {}

    @Override
    public void crawlAll() {}
}
