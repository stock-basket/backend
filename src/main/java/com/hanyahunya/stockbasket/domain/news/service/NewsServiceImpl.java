package com.hanyahunya.stockbasket.domain.news.service;
import com.hanyahunya.stockbasket.domain.news.dto.NewsDetailResponse;
import com.hanyahunya.stockbasket.domain.news.dto.NewsResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NewsServiceImpl implements NewsService {

    @Override
    public List<NewsResponse> getNewsByUser(UUID userId) {
        return List.of();
    }

    @Override
    public List<NewsResponse> getNewsByUserAndSentiment(UUID userId, String sentimentType) {
        return List.of();
    }

    @Override
    public NewsDetailResponse getNewsDetail(Long newsId) {
        return null;
    }

    @Override
    public List<NewsResponse> getUrgentNews(UUID userId) {
        return List.of();
    }

    @Override
    public List<NewsResponse> getNewsByStock(String stockCode) {
        return List.of();
    }
}
