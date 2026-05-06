package com.hanyahunya.stockbasket.domain.news.service;

import com.hanyahunya.stockbasket.domain.analysis.entity.SentimentType;
import com.hanyahunya.stockbasket.domain.news.NewsErrorCode;
import com.hanyahunya.stockbasket.domain.news.dto.NewsDetailResponse;
import com.hanyahunya.stockbasket.domain.news.dto.NewsResponse;
import com.hanyahunya.stockbasket.domain.news.entity.News;
import com.hanyahunya.stockbasket.domain.news.repository.NewsRepository;
import com.hanyahunya.stockbasket.global.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NewsServiceImpl implements NewsService {

    private final NewsRepository newsRepository;

    @Override
    public List<NewsResponse> getNewsByUser(UUID userId) {
        return newsRepository.findAll().stream()
                .map(this::toNewsResponse)
                .toList();
    }

    @Override
    public List<NewsResponse> getNewsByUserAndSentiment(UUID userId, String sentimentType) {
        SentimentType target;

        try {
            target = SentimentType.valueOf(sentimentType.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BusinessException(NewsErrorCode.INVALID_SENTIMENT_TYPE);
        }

        return getNewsByUser(userId).stream()
                .filter(news -> news.sentimentType() == target)
                .toList();
    }

    @Override
    public NewsDetailResponse getNewsDetail(Long newsId) {
        Optional<News> byId = newsRepository.findById(newsId);
        if (byId.isEmpty()) {
            throw new BusinessException(NewsErrorCode.NEWS_NOT_FOUND);
        }

        News news = byId.get();

//        return toNewsDetailResponse(news);
        return null;
    }

    @Override
    public List<NewsResponse> getUrgentNews(UUID userId) {
        return getNewsByUser(userId).stream()
                .filter(news -> news.impactScore() >= 80)
                .toList();
    }

    @Override
    public List<NewsResponse> getNewsByStock(String stockCode) {
        return newsRepository.findAllByStock_StockCodeOrderByPublishedAtDesc(stockCode)
                .stream()
                .map(this::toNewsResponse)
                .toList();
    }

    private NewsResponse toNewsResponse(News news) {
        return new NewsResponse(
                news.getId(),
                news.getStock().getStockCode(),
                news.getStock().getName(),
                news.getTitle(),
                null,
                news.getSourceUrl(),
                news.getPublishedAt(),
                null,
                0,
                null
        );
    }

//    private NewsDetailResponse toNewsDetailResponse(News news) {
//        return new NewsDetailResponse(
//                news.getId(),
//                news.getStock().getStockCode(),
//                news.getStock().getName(),
//                news.getTitle(),
//                null,
//                news.getSourceUrl(),
//                news.getPublishedAt(),
//                null,
//                0,
//                null
//        );
//    }
}