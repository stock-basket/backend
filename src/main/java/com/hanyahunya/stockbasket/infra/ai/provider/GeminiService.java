package com.hanyahunya.stockbasket.infra.ai.provider;

import com.hanyahunya.stockbasket.infra.ai.LLMType;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@NoArgsConstructor(access = AccessLevel.PACKAGE)
public class GeminiService extends AbstractLLMService {

    @Value("${llm.gemini.api-key:}")
    private String apiKey;

    @Value("${llm.gemini.model:}")
    private String model;

    @Override
    public String analyze(String prompt) {
        // TODO: Gemini API 호출 구현
        return null;
    }

    @Override
    public LLMType getAiType() {
        return LLMType.GEMINI;
    }
}