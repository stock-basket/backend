package com.hanyahunya.stockbasket.domain.analysis.dto;
import lombok.Builder;
import lombok.Getter;

@Getter @Builder
public class AnalysisResult {
    private String sentimentType;
    private int impactScore;
    private int marketShockScore;
    private int reliabilityScore;
    private int shortTermImpact;
    private int longTermImpact;
    private String aiAnalysis;
    private String aiVerdict;
}
