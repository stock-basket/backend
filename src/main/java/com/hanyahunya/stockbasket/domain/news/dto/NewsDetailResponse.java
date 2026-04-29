package com.hanyahunya.stockbasket.domain.news.dto;

import com.hanyahunya.stockbasket.domain.analysis.entity.SentimentType;

import java.time.LocalDateTime;

/** 뉴스 상세 (분석 결과 포함) 응답 DTO */
public record NewsDetailResponse(
        Long          id,
        String        stockCode,
        String        stockName,
        String        title,
        String        content,
        String        sourceName,
        String        sourceUrl,
        LocalDateTime publishedAt,

        // 분석 결과
        SentimentType sentimentType,
        int           impactScore,
        int           marketShockScore,
        int           reliabilityScore,
        int           shortTermImpact,
        int           longTermImpact,
        String        aiAnalysis,
        String        aiVerdict
) {}
