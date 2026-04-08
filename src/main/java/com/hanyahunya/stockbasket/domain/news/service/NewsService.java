package com.hanyahunya.stockbasket.domain.news.service;
import com.hanyahunya.stockbasket.domain.news.dto.NewsDetailResponse;
import com.hanyahunya.stockbasket.domain.news.dto.NewsResponse;
import java.util.List;

public interface NewsService {
    List<NewsResponse> getNewsByStock(String stockId);


    List<NewsResponse> getNewsByUser(Long userId);
    List<NewsResponse> getNewsByUserAndSentiment(Long userId, String sentimentType);
    NewsDetailResponse getNewsDetail(Long newsId);
    List<NewsResponse> getUrgentNews(Long userId);
}
