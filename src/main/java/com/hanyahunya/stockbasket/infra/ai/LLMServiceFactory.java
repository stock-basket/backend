package com.hanyahunya.stockbasket.infra.ai;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class LLMServiceFactory {
    private final Map<LLMType, LLMService> services;

    public LLMServiceFactory(List<LLMService> services) {
        this.services = services.stream()
                .collect(Collectors.toMap(
                        LLMService::getAiType,
                        service -> service
                ));
    }

    public LLMService getService(LLMType type) {
        LLMService service = services.get(type);
        if (service == null) {
            throw new IllegalStateException("No service found for type " + type);
        }
        return service;
    }
}
