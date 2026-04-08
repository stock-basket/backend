package com.hanyahunya.stockbasket.api;
import com.hanyahunya.stockbasket.domain.news.dto.*;
import com.hanyahunya.stockbasket.domain.news.service.NewsService;
import com.hanyahunya.stockbasket.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/news")
@RequiredArgsConstructor
public class NewsController {

    private final NewsService newsService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<NewsResponse>>> getMyNews(@AuthenticationPrincipal Long userId,
                                                                      @RequestParam(required = false) String sentiment) {
        if (sentiment != null) {
            return ResponseEntity.ok(ApiResponse.ok(newsService.getNewsByUserAndSentiment(userId, sentiment)));
        }
        return ResponseEntity.ok(ApiResponse.ok(newsService.getNewsByUser(userId)));
    }

    @GetMapping("/{newsId}")
    public ResponseEntity<ApiResponse<NewsDetailResponse>> getNewsDetail(@PathVariable Long newsId) {
        return ResponseEntity.ok(ApiResponse.ok(newsService.getNewsDetail(newsId)));
    }

    @GetMapping("/urgent")
    public ResponseEntity<ApiResponse<List<NewsResponse>>> getUrgentNews(@AuthenticationPrincipal Long userId) {
        return ResponseEntity.ok(ApiResponse.ok(newsService.getUrgentNews(userId)));
    }

    @GetMapping("/stock/{stockId}")
    public ResponseEntity<ApiResponse<List<NewsResponse>>> getNewsByStock(@PathVariable String stockId) {
        return ResponseEntity.ok(ApiResponse.ok(newsService.getNewsByStock(stockId)));
    }
}
