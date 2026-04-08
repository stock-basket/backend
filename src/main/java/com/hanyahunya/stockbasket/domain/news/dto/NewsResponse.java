package com.hanyahunya.stockbasket.domain.news.dto;
import com.hanyahunya.stockbasket.domain.analysis.entity.SentimentType;
import lombok.Builder;
import java.time.LocalDateTime;

@Builder
public record NewsResponse(
        Long id,
        String stockCode, // 종목코드
        String stockName, // 종목명
        String title, // 제목
        String sourceName, // ?
        String sourceUrl, // 뉴스 원본 주소
        LocalDateTime publishedAt, // 뉴스 작성 시간
        SentimentType sentimentType, // 호재 악재 중립 여부
        int impactScore, // 영향력
        String aiComment
) {
}