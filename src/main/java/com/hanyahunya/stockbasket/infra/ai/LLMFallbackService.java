package com.hanyahunya.stockbasket.infra.ai;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class LLMFallbackService {

    private static final List<LLMType> FALLBACK_ORDER = List.of(
            LLMType.CLAUDE, LLMType.OPEN_AI, LLMType.GEMINI, LLMType.NVIDIA
    );

    private final LLMServiceFactory llmServiceFactory;
    private final LLMProperties llmProperties;

    public String analyze(String prompt) {
        for (LLMType type : FALLBACK_ORDER) {
            String key = type.name().toLowerCase().replace("_", "");
            if (type == LLMType.OPEN_AI) key = "openai";

            if (!llmProperties.isEnabled(key)) {
                log.debug("[LLMFallback] {} 비활성화, 스킵", type);
                continue;
            }

            try {
                LLMService service = llmServiceFactory.getService(type);
                String result = service.analyze(prompt);
                if (result != null && !result.isBlank()) {
                    log.info("[LLMFallback] {} 분석 성공", type);
                    return result;
                }
                log.warn("[LLMFallback] {} 빈 응답, 다음 provider 시도", type);
            } catch (Exception e) {
                log.warn("[LLMFallback] {} 실패 ({}), 다음 provider 시도", type, e.getMessage());
            }
        }
        log.error("[LLMFallback] 모든 LLM provider 실패");
        return null;
    }
}
