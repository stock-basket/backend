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
public class NvidiaService extends AbstractLLMService {

    @Value("${llm.nvidia.api-key:}")
    private String apiKey;

    @Value("${llm.nvidia.model:meta/llama-3.1-405b-instruct}")
    private String model;

    @Value("${llm.nvidia.base-url:https://integrate.api.nvidia.com/v1}")
    private String baseUrl;

    @Override
    public String analyze(String prompt) {
        if (apiKey == null || apiKey.isBlank()) return null;

        Map<String, Object> body = Map.of(
                "model", model,
                "messages", List.of(
                        Map.of("role", "system", "content", SYSTEM_PROMPT),
                        Map.of("role", "user", "content", prompt)
                )
        );

        String response = RestClient.create()
                .post()
                .uri(baseUrl + "/chat/completions")
                .header("Authorization", "Bearer " + apiKey)
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .body(String.class);

        return parseChatCompletionResponse(response);
    }

    private String parseChatCompletionResponse(String response) {
        if (response == null) return null;
        try {
            tools.jackson.databind.ObjectMapper mapper = new tools.jackson.databind.ObjectMapper();
            tools.jackson.databind.JsonNode root = mapper.readTree(response);
            tools.jackson.databind.JsonNode content = root.path("choices").path(0).path("message").path("content");
            if (!content.isMissingNode()) {
                return extractJson(content.asText());
            }
        } catch (Exception e) {
            log.warn("[NVIDIA] 응답 파싱 실패: {}", e.getMessage());
        }
        return null;
    }

    @Override
    public LLMType getAiType() {
        return LLMType.NVIDIA;
    }
}
