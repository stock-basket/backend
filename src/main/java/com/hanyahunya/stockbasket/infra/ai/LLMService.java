package com.hanyahunya.stockbasket.infra.ai;

public interface LLMService {
    String analyze(String prompt);

    LLMType getAiType();
}
