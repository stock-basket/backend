package com.hanyahunya.stockbasket.infra.ai.provider;

import com.hanyahunya.stockbasket.infra.ai.LLMType;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@NoArgsConstructor(access = AccessLevel.PACKAGE)
public class ClaudeService extends AbstractLLMService {

    @Value("${claude.api-key:}")
    private String apiKey;

    @Value("${claude.model:}")
    private String model;

    @Override
    public String analyze(String prompt) {
        // TODO: Claude API 호출 구현
        return null;
    }

    @Override
    public LLMType getAiType() {
        return LLMType.CLAUDE;
    }
}