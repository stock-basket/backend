package com.hanyahunya.stockbasket.infra.ai;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class OpenAiClient {

    @Value("${openai.api-key}")
    private String apiKey;

    @Value("${openai.model:gpt-4o}")
    private String model;

    public String analyze(String prompt) {
        // TODO: OpenAI API 호출 구현
        return null;
    }
}
