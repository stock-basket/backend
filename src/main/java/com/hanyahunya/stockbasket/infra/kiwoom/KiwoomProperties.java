package com.hanyahunya.stockbasket.infra.kiwoom;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "kiwoom")
public record KiwoomProperties(
        String tokenUrl,
        String wsUrl,
        String appkey,
        String secretkey
) {}
