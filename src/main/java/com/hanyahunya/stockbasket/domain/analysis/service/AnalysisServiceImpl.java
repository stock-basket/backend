package com.hanyahunya.stockbasket.domain.analysis.service;
import com.hanyahunya.stockbasket.domain.analysis.dto.AnalysisRequest;
import com.hanyahunya.stockbasket.domain.analysis.dto.AnalysisResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AnalysisServiceImpl implements AnalysisService {

    @Override
    public AnalysisResult analyze(AnalysisRequest request) { return null; }

    @Override
    public void analyzeAndSave(Long newsId) {}

    @Override
    public void analyzeAllPending() {}
}
