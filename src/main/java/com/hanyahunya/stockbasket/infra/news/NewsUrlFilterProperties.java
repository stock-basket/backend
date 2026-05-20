package com.hanyahunya.stockbasket.infra.news;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "naver.search.url-filter")
public class NewsUrlFilterProperties {

    private boolean enabled = false;
    private List<String> allowedPatterns = List.of("https://n.news.naver.com");

    public boolean isAllowed(String url) {
        if (!enabled) return true;
        return allowedPatterns.stream().anyMatch(url::startsWith);
    }
}
