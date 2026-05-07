package com.hanyahunya.stockbasket.domain.analysis.service;

import com.hanyahunya.stockbasket.domain.analysis.dto.AnalysisRequest;
import com.hanyahunya.stockbasket.domain.analysis.dto.AnalysisResult;
import com.hanyahunya.stockbasket.domain.analysis.entity.NewsAnalysis;
import com.hanyahunya.stockbasket.domain.analysis.entity.SentimentType;
import com.hanyahunya.stockbasket.domain.analysis.repository.NewsAnalysisRepository;
import com.hanyahunya.stockbasket.domain.news.entity.News;
import com.hanyahunya.stockbasket.domain.news.repository.NewsRepository;
import com.hanyahunya.stockbasket.infra.ai.LLMFallbackService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnalysisServiceImpl implements AnalysisService {

    private final NewsRepository newsRepository;
    private final NewsAnalysisRepository newsAnalysisRepository;
    private final LLMFallbackService llmFallbackService;

    @Lazy
    @Autowired
    private AnalysisService self;

    @Override
    public AnalysisResult analyze(AnalysisRequest request) {
        return null;
    }

    @Async("newsAnalysisExecutor")
    @Transactional
    @Override
    public void analyzeAndSave(Long newsId) {
        if (newsAnalysisRepository.findByNews_Id(newsId).isPresent()) return;

        News news = newsRepository.findById(newsId).orElse(null);
        if (news == null) return;

        String prompt = buildPrompt(news);
        String jsonResponse = llmFallbackService.analyze(prompt);
        if (jsonResponse == null) {
            log.warn("[AnalysisService] 뉴스 ID {} 분석 실패 - 모든 LLM 응답 없음", newsId);
            return;
        }

        try {
            tools.jackson.databind.ObjectMapper mapper = new tools.jackson.databind.ObjectMapper();
            tools.jackson.databind.JsonNode node = mapper.readTree(jsonResponse);

            String sentimentRaw = node.path("sentimentType").asText("NEUTRAL");
            SentimentType sentimentType;
            try {
                sentimentType = SentimentType.valueOf(sentimentRaw.toUpperCase());
            } catch (IllegalArgumentException e) {
                sentimentType = SentimentType.NEUTRAL;
            }

            String keywordsJson = node.path("keywords").isMissingNode()
                    ? "[]" : mapper.writeValueAsString(node.path("keywords"));
            String keyPointsJson = node.path("keyPoints").isMissingNode()
                    ? "[]" : mapper.writeValueAsString(node.path("keyPoints"));

            NewsAnalysis analysis = NewsAnalysis.builder()
                    .news(news)
                    .sentimentType(sentimentType)
                    .impactScore(node.path("impactScore").asInt(0))
                    .marketShockScore(node.path("marketShockScore").asInt(0))
                    .reliabilityScore(node.path("reliabilityScore").asInt(0))
                    .shortTermImpact(node.path("shortTermImpact").asInt(0))
                    .longTermImpact(node.path("longTermImpact").asInt(0))
                    .aiAnalysis(node.path("aiAnalysis").asText(null))
                    .aiVerdict(node.path("aiVerdict").asText(null))
                    .keywords(keywordsJson)
                    .keyPoints(keyPointsJson)
                    .build();

            newsAnalysisRepository.save(analysis);
            log.info("[AnalysisService] 뉴스 ID {} 분석 완료 ({})", newsId, sentimentType);

        } catch (Exception e) {
            log.error("[AnalysisService] 뉴스 ID {} JSON 파싱 실패: {}", newsId, e.getMessage());
        }
    }

    @Override
    public void analyzeAllPending() {
        List<News> pending = newsRepository.findAllWithoutAnalysis();
        log.info("[AnalysisService] 미분석 뉴스 {}건 분석 시작", pending.size());
        pending.forEach(news -> self.analyzeAndSave(news.getId()));
    }

    private String buildPrompt(News news) {
        String content = news.getContent();
        if (content != null && content.length() > 2000) {
            content = content.substring(0, 2000);
        }
        return String.format("""
                종목명: %s
                뉴스 제목: %s
                뉴스 본문:
                %s
                """,
                news.getStock().getName(),
                news.getTitle(),
                content != null ? content : "본문 없음"
        );
    }
}
