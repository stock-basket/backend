package com.hanyahunya.stockbasket.infra.news;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.client.RestClient;

@Configuration
@EnableScheduling
@EnableConfigurationProperties(NaverNewsProperties.class)
public class NewsIngestionConfig {

    @Bean("naverNewsRestClient")
    public RestClient naverNewsRestClient() {
        return RestClient.builder()
                .defaultHeader("Accept", "application/json")
                .build();
    }
}
