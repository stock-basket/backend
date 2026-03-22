package com.hanyahunya.stockbasket.domain.analysis.dto;
import lombok.Getter;

@Getter
public class AnalysisRequest {
    private Long newsId;
    private String title;
    private String content;
    private String stockName;
}
