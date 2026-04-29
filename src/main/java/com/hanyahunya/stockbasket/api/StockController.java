package com.hanyahunya.stockbasket.api;

import com.hanyahunya.stockbasket.domain.stock.dto.StockAddRequest;
import com.hanyahunya.stockbasket.domain.stock.dto.StockResponse;
import com.hanyahunya.stockbasket.domain.stock.service.StockService;
import com.hanyahunya.stockbasket.global.response.ApiResponse;
import com.hanyahunya.stockbasket.global.security.UserPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 종목(바구니) 관련 엔드포인트.
 *
 * <p>모든 요청: Authorization: Bearer {access_token} 필요.
 */
@RestController
@RequestMapping("/api/stocks")
@RequiredArgsConstructor
public class StockController {

    private final StockService stockService;

    /**
     * 내 바구니 종목 목록 조회.
     *
     * <pre>GET /api/stocks/basket</pre>
     */
    @GetMapping("/basket")
    public ResponseEntity<ApiResponse<List<StockResponse>>> getMyBasket(
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ResponseEntity.ok(ApiResponse.ok(stockService.getMyBasket(principal.getUserId())));
    }

    /**
     * 바구니에 종목 추가.
     *
     * <pre>POST /api/stocks/basket</pre>
     * <ul>
     *   <li>실패: 404 — 존재하지 않는 종목 코드</li>
     *   <li>실패: 409 — 이미 바구니에 담긴 종목</li>
     *   <li>실패: 400 — 바구니 한도 초과</li>
     * </ul>
     */
    @PostMapping("/basket")
    public ResponseEntity<ApiResponse<Void>> addToBasket(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestBody @Valid StockAddRequest request
    ) {
        stockService.addToBasket(principal.getUserId(), request);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    /**
     * 바구니에서 종목 제거.
     *
     * <pre>DELETE /api/stocks/basket/{stockCode}</pre>
     * <ul>
     *   <li>실패: 404 — 존재하지 않는 종목</li>
     * </ul>
     */
    @DeleteMapping("/basket/{stockCode}")
    public ResponseEntity<ApiResponse<Void>> removeFromBasket(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable String stockCode
    ) {
        stockService.removeFromBasket(principal.getUserId(), stockCode);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    /**
     * 종목 상세 조회 (뉴스 요약 포함).
     *
     * <pre>GET /api/stocks/{stockCode}</pre>
     * <ul>
     *   <li>실패: 404 — 존재하지 않는 종목</li>
     * </ul>
     */
    @GetMapping("/{stockCode}")
    public ResponseEntity<ApiResponse<StockResponse>> getStockDetail(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable String stockCode
    ) {
        return ResponseEntity.ok(ApiResponse.ok(stockService.getStockDetail(principal.getUserId(), stockCode)));
    }

    /**
     * 종목 검색 (종목명 또는 종목코드).
     *
     * <pre>GET /api/stocks/search?keyword={keyword}</pre>
     */
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<StockResponse>>> search(
            @RequestParam String keyword
    ) {
        return ResponseEntity.ok(ApiResponse.ok(stockService.searchStocks(keyword)));
    }
}
