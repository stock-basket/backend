package com.hanyahunya.stockbasket.domain.news.dto;

import com.hanyahunya.stockbasket.domain.analysis.entity.SentimentType;

import java.time.LocalDateTime;

/** 뉴스 목록 카드용 응답 DTO */
public record NewsResponse(
        Long          id,
        String        stockCode,
        String        stockName,
        String        title,
        String        publisher,
        String        sourceUrl,
        LocalDateTime publishedAt,
        // 이 밑은 null 이나 0 이면 아직 ai 분석이 진행중인 상황
        SentimentType sentimentType,   // POSITIVE | NEGATIVE | NEUTRAL
        int           impactScore,     // 0 ~ 100
        String        aiComment        // 한 줄 요약
) {
}