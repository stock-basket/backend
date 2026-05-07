package com.hanyahunya.stockbasket.infra.ai.provider;

import com.hanyahunya.stockbasket.infra.ai.LLMService;

public abstract class AbstractLLMService implements LLMService {
    protected static final String SYSTEM_PROMPT = """
            당신은 한국 주식시장 뉴스 분석 전문가입니다.
            주어진 뉴스 기사를 분석하고 반드시 아래 JSON 형식으로만 응답하세요. 다른 텍스트는 절대 포함하지 마세요.

            {
              "sentimentType": "POSITIVE 또는 NEGATIVE 또는 NEUTRAL 중 하나",
              "impactScore": 주가 영향력 점수 0에서 100 사이 정수,
              "marketShockScore": 시장 충격도 0에서 100 사이 정수,
              "reliabilityScore": 정보 신뢰도 0에서 100 사이 정수,
              "shortTermImpact": 단기(1주일 이내) 영향도 0에서 100 사이 정수,
              "longTermImpact": 장기(1개월 이상) 영향도 0에서 100 사이 정수,
              "aiAnalysis": "해당 종목 주가에 미치는 영향 상세 분석 2~3문단 한국어",
              "aiVerdict": "뉴스가 주가에 미치는 영향 한 문장 요약 한국어",
              "keywords": ["관련 키워드1", "키워드2", "최대 5개"],
              "keyPoints": [
                {"text": "핵심 포인트 설명 한국어", "sentiment": "POSITIVE 또는 NEGATIVE 또는 NEUTRAL 중 하나"},
                최대 5개
              ]
            }
            """;

    protected String extractJson(String text) {
        if (text == null) return null;
        text = text.replaceAll("```json\\s*", "").replaceAll("```\\s*", "").trim();
        int start = text.indexOf('{');
        int end = text.lastIndexOf('}');
        if (start >= 0 && end > start) {
            return text.substring(start, end + 1);
        }
        return text;
    }
}
