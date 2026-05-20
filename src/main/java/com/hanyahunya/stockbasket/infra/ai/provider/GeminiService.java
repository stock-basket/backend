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
public class GeminiService extends AbstractLLMService {

    @Value("${llm.gemini.api-key:}")
    private String apiKey;

    @Value("${llm.gemini.model:gemini-2.0-flash}")
    private String model;

    @Override
    public String analyze(String prompt) {
        if (apiKey == null || apiKey.isBlank()) return null;

        Map<String, Object> body = Map.of(
                "contents", List.of(
                        Map.of("parts", List.of(
                                Map.of("text", SYSTEM_PROMPT + "\n\n" + prompt)
                        ))
                )
        );

        String response = RestClient.create()
                .post()
                .uri("https://generativelanguage.googleapis.com/v1beta/models/{model}:generateContent?key={key}",
                        model, apiKey)
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .body(String.class);

        return parseGeminiResponse(response);
    }

    private String parseGeminiResponse(String response) {
        if (response == null) return null;
        try {
            tools.jackson.databind.ObjectMapper mapper = new tools.jackson.databind.ObjectMapper();
            tools.jackson.databind.JsonNode root = mapper.readTree(response);
            tools.jackson.databind.JsonNode text = root
                    .path("candidates").path(0)
                    .path("content").path("parts").path(0)
                    .path("text");
            if (!text.isMissingNode()) {
                return extractJson(text.asText());
            }
        } catch (Exception e) {
            log.warn("[Gemini] 응답 파싱 실패: {}", e.getMessage());
        }
        return null;
    }

    @Override
    public LLMType getAiType() {
        return LLMType.GEMINI;
    }
}