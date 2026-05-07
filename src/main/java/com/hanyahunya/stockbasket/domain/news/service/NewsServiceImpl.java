package com.hanyahunya.stockbasket.domain.news.service;

import com.hanyahunya.stockbasket.domain.analysis.entity.NewsAnalysis;
import com.hanyahunya.stockbasket.domain.analysis.entity.SentimentType;
import com.hanyahunya.stockbasket.domain.analysis.repository.NewsAnalysisRepository;
import com.hanyahunya.stockbasket.domain.news.NewsErrorCode;
import com.hanyahunya.stockbasket.domain.news.dto.NewsDetailResponse;
import com.hanyahunya.stockbasket.domain.news.dto.NewsResponse;
import com.hanyahunya.stockbasket.domain.news.entity.News;
import com.hanyahunya.stockbasket.domain.news.repository.NewsRepository;
import com.hanyahunya.stockbasket.global.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NewsServiceImpl implements NewsService {

    private final NewsRepository newsRepository;
    private final NewsAnalysisRepository newsAnalysisRepository;

    @Override
    public Page<NewsResponse> getNewsByUser(UUID userId, Pageable pageable) {
        return convertToNewsResponsePage(newsRepository.findNewsByUserId(userId, pageable));
    }

    @Override
    public Page<NewsResponse> getNewsByUserAndSentiment(UUID userId, String sentimentType, Pageable pageable) {
        SentimentType target;
        try {
            target = SentimentType.valueOf(sentimentType.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BusinessException(NewsErrorCode.INVALID_SENTIMENT_TYPE);
        }
        return convertToNewsResponsePage(
                newsRepository.findNewsByUserIdAndSentiment(userId, target, pageable));
    }

    @Override
    public Page<NewsResponse> getUrgentNews(UUID userId, Pageable pageable) {
        return convertToNewsResponsePage(newsRepository.findUrgentNewsByUserId(userId, pageable));
    }

    @Override
    public Page<NewsResponse> getNewsByStock(String stockCode, Pageable pageable) {
        return convertToNewsResponsePage(
                newsRepository.findAllByStock_StockCodeOrderByPublishedAtDesc(stockCode, pageable));
    }

    @Override
    public NewsDetailResponse getNewsDetail(Long newsId) {
        News news = newsRepository.findById(newsId)
                .orElseThrow(() -> new BusinessException(NewsErrorCode.NEWS_NOT_FOUND));
        NewsAnalysis na = newsAnalysisRepository.findByNews_Id(newsId).orElse(null);
        return toDetailResponse(news, na);
    }

    private Page<NewsResponse> convertToNewsResponsePage(Page<News> newsPage) {
        if (newsPage.isEmpty()) {
            return Page.empty(newsPage.getPageable());
        }
        List<Long> newsIds = newsPage.getContent().stream()
                .map(News::getId)
                .toList();
        Map<Long, NewsAnalysis> analysisMap = newsAnalysisRepository.findByNews_IdIn(newsIds)
                .stream()
                .collect(Collectors.toMap(na -> na.getNews().getId(), na -> na));
        return newsPage.map(news -> {
            NewsAnalysis na = analysisMap.get(news.getId());
            return new NewsResponse(
                    news.getId(),
                    news.getStock().getStockCode(),
                    news.getStock().getName(),
                    news.getTitle(),
                    news.getPublisher(),
                    news.getSourceUrl(),
                    news.getPublishedAt(),
                    na != null ? na.getSentimentType() : null,
                    na != null ? na.getImpactScore() : 0,
                    na != null ? na.getAiVerdict() : null
            );
        });
    }

    private NewsDetailResponse toDetailResponse(News news, NewsAnalysis na) {
        return new NewsDetailResponse(
                news.getId(),
                news.getStock().getStockCode(),
                news.getStock().getName(),
                news.getTitle(),
                news.getContent(),
                news.getPublisher(),
                news.getSourceUrl(),
                news.getPublishedAt(),
                na != null ? na.getSentimentType() : null,
                na != null ? na.getImpactScore() : 0,
                na != null ? na.getMarketShockScore() : 0,
                na != null ? na.getReliabilityScore() : 0,
                na != null ? na.getShortTermImpact() : 0,
                na != null ? na.getLongTermImpact() : 0,
                na != null ? na.getAiAnalysis() : null,
                na != null ? na.getAiVerdict() : null,
                na != null ? na.getKeywords() : null,
                na != null ? na.getKeyPoints() : null
        );
    }
}
