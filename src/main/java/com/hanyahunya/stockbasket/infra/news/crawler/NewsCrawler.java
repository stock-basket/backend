package com.hanyahunya.stockbasket.infra.news.crawler;

public interface NewsCrawler {
    /**
     * 주어진 URL의 뉴스 기사를 크롤링합니다.
     *
     * @param url 크롤링할 뉴스 URL
     * @return 크롤링 결과 (언론사, 본문)
     */
    NewsCrawlResult crawl(String url);

    Provider getProvider();
}