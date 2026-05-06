package com.hanyahunya.stockbasket.infra.news.crawler;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@Builder
@ToString
public class NewsCrawlResult {
    private String publisher;
    private String content;
}
