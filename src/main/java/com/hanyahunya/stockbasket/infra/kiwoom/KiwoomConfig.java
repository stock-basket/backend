package com.hanyahunya.stockbasket.infra.kiwoom;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(KiwoomProperties.class)
public class KiwoomConfig {}
