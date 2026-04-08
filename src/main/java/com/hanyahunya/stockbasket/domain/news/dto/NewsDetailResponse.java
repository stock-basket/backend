package com.hanyahunya.stockbasket.domain.news.dto;
import lombok.Builder;
import lombok.Getter;
import java.time.LocalDateTime;

@Getter @Builder
public class NewsDetailResponse {
    private Long id;
    private Long stockId;
    private String stockName;
    private String title;
    private String content;
    private String sourceName;
    private String sourceUrl;
    private LocalDateTime publishedAt;
    private String sentimentType;
    private int impactScore;
    private int marketShockScore;
    private int reliabilityScore;
    private int shortTermImpact;
    private int longTermImpact;
    private String aiAnalysis;
    private String aiVerdict;
}
