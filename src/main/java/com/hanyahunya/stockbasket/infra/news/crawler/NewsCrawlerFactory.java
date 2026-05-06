package com.hanyahunya.stockbasket.infra.news.crawler;

import com.hanyahunya.stockbasket.global.exception.ErrorCode;
import com.hanyahunya.stockbasket.global.exception.ExternalException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class NewsCrawlerFactory {
    private final Map<Provider, NewsCrawler> crawlers;

    public NewsCrawlerFactory(List<NewsCrawler> crawlers) {
        this.crawlers = crawlers.stream()
                .collect(Collectors.toMap(
                        NewsCrawler::getProvider,
                        crawler -> crawler
                ));
    }

    public NewsCrawler getNewsCrawler(Provider provider) {
        NewsCrawler newsCrawler = crawlers.get(provider);
        if (newsCrawler == null) {
            throw new ExternalException(CrawlerErrorCode.PROVIDER_NOT_FOUND);
        }
        return newsCrawler;
    }
}
