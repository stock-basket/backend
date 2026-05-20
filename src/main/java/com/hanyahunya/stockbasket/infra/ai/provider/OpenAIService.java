package com.hanyahunya.stockbasket.infra.ai.provider;

import com.hanyahunya.stockbasket.infra.ai.LLMType;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
@NoArgsConstructor(access = AccessLevel.PACKAGE)
public class OpenAIService extends AbstractLLMService {

    @Value("${openai.api-key:}")
    private String apiKey;

    @Value("${openai.model:gpt-4o}")
    private String model;

    @Override
    public String analyze(String prompt) {
        if (apiKey == null || apiKey.isBlank()) return null;
        return callChatCompletions("https://api.openai.com/v1/chat/completions", apiKey, prompt);
    }

    protected String callChatCompletions(String url, String key, String prompt) {
        Map<String, Object> body = Map.of(
                "model", model,
                "messages", List.of(
                        Map.of("role", "system", "content", SYSTEM_PROMPT),
                        Map.of("role", "user", "content", prompt)
                )
        );

        String response = RestClient.create()
                .post()
                .uri(url)
                .header("Authorization", "Bearer " + key)
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .body(String.class);

        return parseChatCompletionResponse(response);
    }

    protected String parseChatCompletionResponse(String response) {
        if (response == null) return null;
        try {
            tools.jackson.databind.ObjectMapper mapper = new tools.jackson.databind.ObjectMapper();
            tools.jackson.databind.JsonNode root = mapper.readTree(response);
            tools.jackson.databind.JsonNode content = root.path("choices").path(0).path("message").path("content");
            if (!content.isMissingNode()) {
                return extractJson(content.asText());
            }
        } catch (Exception e) {
            log.warn("[OpenAI] 응답 파싱 실패: {}", e.getMessage());
        }
        return null;
    }

    @Override
    public LLMType getAiType() {
        return LLMType.OPEN_AI;
    }
}