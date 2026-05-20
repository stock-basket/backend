package com.hanyahunya.stockbasket.domain.news.service;

import com.hanyahunya.stockbasket.domain.analysis.entity.NewsAnalysis;
import com.hanyahunya.stockbasket.domain.analysis.entity.SentimentType;
import com.hanyahunya.stockbasket.domain.analysis.repository.NewsAnalysisRepository;
import com.hanyahunya.stockbasket.domain.news.NewsErrorCode;
import com.hanyahunya.stockbasket.domain.news.dto.NewsDetailResponse;
import com.hanyahunya.stockbasket.domain.news.dto.NewsResponse;
import com.hanyahunya.stockbasket.domain.news.entity.News;
import com.hanyahunya.stockbasket.domain.news.repository.NewsRepository;
import com.hanyahunya.stockbasket.domain.stock.entity.MarketType;
import com.hanyahunya.stockbasket.domain.stock.entity.Stock;
import com.hanyahunya.stockbasket.global.exception.BusinessException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NewsServiceImplTest {

    @Mock NewsRepository newsRepository;
    @Mock NewsAnalysisRepository newsAnalysisRepository;

    @InjectMocks NewsServiceImpl newsService;

    private final Pageable pageable = PageRequest.of(0, 10);

    // ── getNewsByUser ──────────────────────────────────────────────────────────

    @Test
    void getNewsByUser_뉴스_페이지_반환() {
        UUID userId = UUID.randomUUID();
        News news = buildNews(1L, "삼성전자 실적 호조");
        Page<News> newsPage = new PageImpl<>(List.of(news), pageable, 1);
        when(newsRepository.findNewsByUserId(userId, pageable)).thenReturn(newsPage);
        when(newsAnalysisRepository.findByNews_IdIn(List.of(1L))).thenReturn(List.of());

        Page<NewsResponse> result = newsService.getNewsByUser(userId, pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).title()).isEqualTo("삼성전자 실적 호조");
    }

    @Test
    void getNewsByUser_빈_페이지_반환() {
        UUID userId = UUID.randomUUID();
        Page<News> empty = Page.empty(pageable);
        when(newsRepository.findNewsByUserId(userId, pageable)).thenReturn(empty);

        Page<NewsResponse> result = newsService.getNewsByUser(userId, pageable);

        assertThat(result.isEmpty()).isTrue();
    }

    @Test
    void getNewsByUser_분석포함_뉴스_반환() {
        UUID userId = UUID.randomUUID();
        News news = buildNews(1L, "호재 뉴스");
        NewsAnalysis analysis = buildAnalysis(news, SentimentType.POSITIVE, 85);
        Page<News> newsPage = new PageImpl<>(List.of(news), pageable, 1);
        when(newsRepository.findNewsByUserId(userId, pageable)).thenReturn(newsPage);
        when(newsAnalysisRepository.findByNews_IdIn(List.of(1L))).thenReturn(List.of(analysis));

        Page<NewsResponse> result = newsService.getNewsByUser(userId, pageable);

        NewsResponse response = result.getContent().get(0);
        assertThat(response.sentimentType()).isEqualTo(SentimentType.POSITIVE);
        assertThat(response.impactScore()).isEqualTo(85);
    }

    // ── getNewsByUserAndSentiment ──────────────────────────────────────────────

    @Test
    void getNewsByUserAndSentiment_POSITIVE_필터링() {
        UUID userId = UUID.randomUUID();
        News news = buildNews(1L, "긍정 뉴스");
        Page<News> newsPage = new PageImpl<>(List.of(news), pageable, 1);
        when(newsRepository.findNewsByUserIdAndSentiment(userId, SentimentType.POSITIVE, pageable))
                .thenReturn(newsPage);
        when(newsAnalysisRepository.findByNews_IdIn(anyList())).thenReturn(List.of());

        Page<NewsResponse> result = newsService.getNewsByUserAndSentiment(userId, "POSITIVE", pageable);

        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    void getNewsByUserAndSentiment_소문자도_처리() {
        UUID userId = UUID.randomUUID();
        Page<News> empty = new PageImpl<>(List.of(), pageable, 0);
        when(newsRepository.findNewsByUserIdAndSentiment(userId, SentimentType.NEGATIVE, pageable))
                .thenReturn(empty);

        assertThatNoException().isThrownBy(() ->
                newsService.getNewsByUserAndSentiment(userId, "negative", pageable));
    }

    @Test
    void getNewsByUserAndSentiment_유효하지않은_타입_INVALID_SENTIMENT_TYPE_예외() {
        UUID userId = UUID.randomUUID();

        assertThatThrownBy(() ->
                newsService.getNewsByUserAndSentiment(userId, "UNKNOWN_TYPE", pageable))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode())
                        .isEqualTo(NewsErrorCode.INVALID_SENTIMENT_TYPE));
    }

    // ── getUrgentNews ──────────────────────────────────────────────────────────

    @Test
    void getUrgentNews_긴급뉴스_반환() {
        UUID userId = UUID.randomUUID();
        News news = buildNews(1L, "긴급 시장 이슈");
        Page<News> newsPage = new PageImpl<>(List.of(news), pageable, 1);
        when(newsRepository.findUrgentNewsByUserId(userId, pageable)).thenReturn(newsPage);
        when(newsAnalysisRepository.findByNews_IdIn(anyList())).thenReturn(List.of());

        Page<NewsResponse> result = newsService.getUrgentNews(userId, pageable);

        assertThat(result.getContent()).hasSize(1);
    }

    // ── getNewsByStock ─────────────────────────────────────────────────────────

    @Test
    void getNewsByStock_종목별_뉴스_반환() {
        News news = buildNews(1L, "삼성전자 관련 뉴스");
        Page<News> newsPage = new PageImpl<>(List.of(news), pageable, 1);
        when(newsRepository.findAllByStock_StockCodeOrderByPublishedAtDesc("005930", pageable))
                .thenReturn(newsPage);
        when(newsAnalysisRepository.findByNews_IdIn(anyList())).thenReturn(List.of());

        Page<NewsResponse> result = newsService.getNewsByStock("005930", pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).stockCode()).isEqualTo("005930");
    }

    // ── getNewsDetail ──────────────────────────────────────────────────────────

    @Test
    void getNewsDetail_분석포함_상세_반환() {
        News news = buildNews(1L, "상세 뉴스");
        NewsAnalysis analysis = buildAnalysis(news, SentimentType.POSITIVE, 90);
        when(newsRepository.findById(1L)).thenReturn(Optional.of(news));
        when(newsAnalysisRepository.findByNews_Id(1L)).thenReturn(Optional.of(analysis));

        NewsDetailResponse response = newsService.getNewsDetail(1L);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.title()).isEqualTo("상세 뉴스");
        assertThat(response.sentimentType()).isEqualTo(SentimentType.POSITIVE);
        assertThat(response.impactScore()).isEqualTo(90);
    }

    @Test
    void getNewsDetail_분석없이_상세_반환() {
        News news = buildNews(1L, "미분석 뉴스");
        when(newsRepository.findById(1L)).thenReturn(Optional.of(news));
        when(newsAnalysisRepository.findByNews_Id(1L)).thenReturn(Optional.empty());

        NewsDetailResponse response = newsService.getNewsDetail(1L);

        assertThat(response.sentimentType()).isNull();
        assertThat(response.impactScore()).isZero();
        assertThat(response.aiAnalysis()).isNull();
    }

    @Test
    void getNewsDetail_미존재_뉴스_NEWS_NOT_FOUND_예외() {
        when(newsRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> newsService.getNewsDetail(999L))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode())
                        .isEqualTo(NewsErrorCode.NEWS_NOT_FOUND));
    }

    // ── helpers ────────────────────────────────────────────────────────────────

    private News buildNews(Long id, String title) {
        Stock stock = Stock.builder()
                .stockCode("005930").name("삼성전자").market(MarketType.KOSPI).build();
        News news = News.builder()
                .stock(stock)
                .title(title)
                .content("뉴스 본문")
                .sourceUrl("https://example.com/news/" + id)
                .publisher("연합뉴스")
                .publishedAt(LocalDateTime.now())
                .build();
        ReflectionTestUtils.setField(news, "id", id);
        ReflectionTestUtils.setField(news, "crawledAt", LocalDateTime.now());
        return news;
    }

    private NewsAnalysis buildAnalysis(News news, SentimentType sentiment, int impactScore) {
        NewsAnalysis na = NewsAnalysis.builder()
                .news(news)
                .sentimentType(sentiment)
                .impactScore(impactScore)
                .marketShockScore(70)
                .reliabilityScore(80)
                .shortTermImpact(60)
                .longTermImpact(50)
                .aiAnalysis("AI 분석 결과")
                .aiVerdict("긍정적 전망")
                .build();
        ReflectionTestUtils.setField(na, "analyzedAt", LocalDateTime.now());
        return na;
    }
}
