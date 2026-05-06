package com.hanyahunya.stockbasket.infra.news;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "naver.search")
public record NaverNewsProperties(String clientId, String clientSecret) {
}
