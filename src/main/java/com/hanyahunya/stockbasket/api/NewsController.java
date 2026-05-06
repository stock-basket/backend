package com.hanyahunya.stockbasket.api;

import com.hanyahunya.stockbasket.domain.news.dto.NewsDetailResponse;
import com.hanyahunya.stockbasket.domain.news.dto.NewsResponse;
import com.hanyahunya.stockbasket.domain.news.service.NewsService;
import com.hanyahunya.stockbasket.global.response.ApiResponse;
import com.hanyahunya.stockbasket.global.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * 뉴스 피드 관련 엔드포인트.
 *
 * <p>모든 요청: Authorization: Bearer {access_token} 필요.
 */
@RestController
@RequestMapping("/api/news")
@RequiredArgsConstructor
public class NewsController {

    private final NewsService newsService;

    /**
     * 내 바구니 종목의 뉴스 피드 조회.
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Page<NewsResponse>>> getMyNews(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(required = false) String sentiment,
            @PageableDefault(size = 10) Pageable pageable
    ) {
        Page<NewsResponse> result = (sentiment != null)
                ? newsService.getNewsByUserAndSentiment(principal.getUserId(), sentiment, pageable)
                : newsService.getNewsByUser(principal.getUserId(), pageable);

        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    /**
     * 뉴스 상세 조회 (AI 분석 포함).
     */
    @GetMapping("/{newsId}")
    public ResponseEntity<ApiResponse<NewsDetailResponse>> getNewsDetail(
            @PathVariable Long newsId
    ) {
        return ResponseEntity.ok(ApiResponse.ok(newsService.getNewsDetail(newsId)));
    }

    /**
     * 긴급(고영향) 뉴스 조회.
     *
     * 💡 변경점: List -> Page 로 변경 및 파라미터에 Pageable 추가
     */
    @GetMapping("/urgent")
    public ResponseEntity<ApiResponse<Page<NewsResponse>>> getUrgentNews(
            @AuthenticationPrincipal UserPrincipal principal,
            @PageableDefault(size = 5) Pageable pageable // 긴급 뉴스는 보통 조금씩 보므로 기본 사이즈를 5로 조정
    ) {
        return ResponseEntity.ok(ApiResponse.ok(newsService.getUrgentNews(principal.getUserId(), pageable)));
    }

    /**
     * 특정 종목의 뉴스 목록 조회.
     *
     * 💡 변경점: 특정 종목(예: 삼성전자)의 무한히 쌓이는 뉴스 데이터를 감당하기 위해 Page 로 변경
     */
    @GetMapping("/stock/{stockCode}")
    public ResponseEntity<ApiResponse<Page<NewsResponse>>> getNewsByStock(
            @PathVariable String stockCode,
            @PageableDefault(size = 10) Pageable pageable
    ) {
        return ResponseEntity.ok(ApiResponse.ok(newsService.getNewsByStock(stockCode, pageable)));
    }
}