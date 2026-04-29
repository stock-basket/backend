package com.hanyahunya.stockbasket.api;

import com.hanyahunya.stockbasket.domain.news.dto.NewsDetailResponse;
import com.hanyahunya.stockbasket.domain.news.dto.NewsResponse;
import com.hanyahunya.stockbasket.domain.news.service.NewsService;
import com.hanyahunya.stockbasket.global.response.ApiResponse;
import com.hanyahunya.stockbasket.global.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
     *
     * <pre>GET /api/news[?sentiment=POSITIVE|NEGATIVE|NEUTRAL]</pre>
     * <p>{@code sentiment} 쿼리 파라미터가 없으면 전체를 반환한다.
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<NewsResponse>>> getMyNews(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(required = false) String sentiment
    ) {
        List<NewsResponse> result = (sentiment != null)
                ? newsService.getNewsByUserAndSentiment(principal.getUserId(), sentiment)
                : newsService.getNewsByUser(principal.getUserId());

        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    /**
     * 뉴스 상세 조회 (AI 분석 포함).
     *
     * <pre>GET /api/news/{newsId}</pre>
     * <ul>
     *   <li>실패: 404 — 존재하지 않는 뉴스</li>
     * </ul>
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
     * <pre>GET /api/news/urgent</pre>
     * <p>내 바구니 종목 중 impactScore 가 높거나 가격 알림이 발생한 뉴스만 반환.
     */
    @GetMapping("/urgent")
    public ResponseEntity<ApiResponse<List<NewsResponse>>> getUrgentNews(
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ResponseEntity.ok(ApiResponse.ok(newsService.getUrgentNews(principal.getUserId())));
    }

    /**
     * 특정 종목의 뉴스 목록 조회.
     *
     * <pre>GET /api/news/stock/{stockCode}</pre>
     */
    @GetMapping("/stock/{stockCode}")
    public ResponseEntity<ApiResponse<List<NewsResponse>>> getNewsByStock(
            @PathVariable String stockCode
    ) {
        return ResponseEntity.ok(ApiResponse.ok(newsService.getNewsByStock(stockCode)));
    }
}
