package com.hanyahunya.stockbasket.domain.news.service;

import com.hanyahunya.stockbasket.domain.news.dto.NewsDetailResponse;
import com.hanyahunya.stockbasket.domain.news.dto.NewsResponse;

import java.util.List;
import java.util.UUID;

public interface NewsService {

    /** 내 바구니 종목의 전체 뉴스 피드 (최신순) */
    List<NewsResponse> getNewsByUser(UUID userId);

    /**
     * 내 바구니 종목의 뉴스 피드 — 감성 필터 적용
     *
     * @param sentimentType "POSITIVE" | "NEGATIVE" | "NEUTRAL"
     */
    List<NewsResponse> getNewsByUserAndSentiment(UUID userId, String sentimentType);

    /**
     * 뉴스 상세 조회
     *
     * @throws com.hanyahunya.stockbasket.global.exception.BusinessException
     *         NEWS_NOT_FOUND(404)
     */
    NewsDetailResponse getNewsDetail(Long newsId);

    /**
     * 내 바구니 종목의 긴급(고영향) 뉴스
     * — impactScore 가 임계값 이상이거나 PRICE_SPIKE/DROP 알림이 존재하는 뉴스
     */
    List<NewsResponse> getUrgentNews(UUID userId);

    /** 특정 종목의 뉴스 목록 */
    List<NewsResponse> getNewsByStock(String stockCode);
}
