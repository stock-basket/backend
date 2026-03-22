package com.hanyahunya.stockbasket.domain.news.dto;
import lombok.Builder;
import lombok.Getter;
import java.time.LocalDateTime;

@Getter @Builder
public class NewsResponse {
    private Long id;
    private Long stockId;
    private String stockName;
    private String title;
    private String sourceName;
    private String sourceUrl;
    private LocalDateTime publishedAt;
    private String sentimentType;
    private int impactScore;
    private String aiComment;
}
