package com.hanyahunya.stockbasket.infra.ai;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "llm")
public class LLMProperties {

    private Map<String, Boolean> enabled = new HashMap<>();

    public boolean isEnabled(String providerKey) {
        return enabled.getOrDefault(providerKey, true);
    }
}
