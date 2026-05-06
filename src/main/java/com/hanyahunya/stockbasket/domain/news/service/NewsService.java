package com.hanyahunya.stockbasket.domain.news.service;

import com.hanyahunya.stockbasket.domain.news.dto.NewsDetailResponse;
import com.hanyahunya.stockbasket.domain.news.dto.NewsResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface NewsService {

    Page<NewsResponse> getNewsByUser(UUID userId, Pageable pageable);

    Page<NewsResponse> getNewsByUserAndSentiment(UUID userId, String sentimentType, Pageable pageable);

    NewsDetailResponse getNewsDetail(Long newsId);

    Page<NewsResponse> getUrgentNews(UUID userId, Pageable pageable);

    Page<NewsResponse> getNewsByStock(String stockCode, Pageable pageable);
}
