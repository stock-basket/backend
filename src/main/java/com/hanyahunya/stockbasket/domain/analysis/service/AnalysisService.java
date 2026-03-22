package com.hanyahunya.stockbasket.domain.analysis.service;
import com.hanyahunya.stockbasket.domain.analysis.dto.AnalysisRequest;
import com.hanyahunya.stockbasket.domain.analysis.dto.AnalysisResult;

public interface AnalysisService {
    AnalysisResult analyze(AnalysisRequest request);
    void analyzeAndSave(Long newsId);
    void analyzeAllPending();
}
