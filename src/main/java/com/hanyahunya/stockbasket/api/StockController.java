package com.hanyahunya.stockbasket.api;
import com.hanyahunya.stockbasket.domain.stock.dto.*;
import com.hanyahunya.stockbasket.domain.stock.service.StockService;
import com.hanyahunya.stockbasket.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/stocks")
@RequiredArgsConstructor
public class StockController {

    private final StockService stockService;

    @GetMapping("/basket")
    public ResponseEntity<ApiResponse<List<StockResponse>>> getMyBasket(@AuthenticationPrincipal Long userId) {
        return ResponseEntity.ok(ApiResponse.ok(stockService.getMyBasket(userId)));
    }

    @PostMapping("/basket")
    public ResponseEntity<ApiResponse<Void>> addToBasket(@AuthenticationPrincipal Long userId,
                                                          @RequestBody StockAddRequest request) {
        stockService.addToBasket(userId, request);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @DeleteMapping("/basket/{stockId}")
    public ResponseEntity<ApiResponse<Void>> removeFromBasket(@AuthenticationPrincipal Long userId,
                                                               @PathVariable Long stockId) {
        stockService.removeFromBasket(userId, stockId);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @GetMapping("/{stockId}")
    public ResponseEntity<ApiResponse<StockResponse>> getStockDetail(@AuthenticationPrincipal Long userId,
                                                                       @PathVariable Long stockId) {
        return ResponseEntity.ok(ApiResponse.ok(stockService.getStockDetail(userId, stockId)));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<StockResponse>>> search(@RequestParam String keyword) {
        return ResponseEntity.ok(ApiResponse.ok(stockService.searchStocks(keyword)));
    }
}
