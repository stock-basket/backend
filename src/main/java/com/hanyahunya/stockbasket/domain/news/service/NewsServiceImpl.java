package com.hanyahunya.stockbasket.domain.news.service;
import com.hanyahunya.stockbasket.domain.news.dto.NewsDetailResponse;
import com.hanyahunya.stockbasket.domain.news.dto.NewsResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NewsServiceImpl implements NewsService {

    @Override
    public List<NewsResponse> getNewsByStock(Long stockId) { return List.of(); }

    @Override
    public List<NewsResponse> getNewsByUser(Long userId) { return List.of(); }

    @Override
    public List<NewsResponse> getNewsByUserAndSentiment(Long userId, String sentimentType) { return List.of(); }

    @Override
    public NewsDetailResponse getNewsDetail(Long newsId) { return null; }

    @Override
    public List<NewsResponse> getUrgentNews(Long userId) { return List.of(); }
}
