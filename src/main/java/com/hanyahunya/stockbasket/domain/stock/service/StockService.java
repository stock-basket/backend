package com.hanyahunya.stockbasket.domain.stock.service;
import com.hanyahunya.stockbasket.domain.stock.dto.StockAddRequest;
import com.hanyahunya.stockbasket.domain.stock.dto.StockResponse;
import java.util.List;

public interface StockService {
    void addToBasket(Long userId, StockAddRequest request);
    void removeFromBasket(Long userId, Long stockId);
    List<StockResponse> getMyBasket(Long userId);
    StockResponse getStockDetail(Long userId, Long stockId);
    List<StockResponse> searchStocks(String keyword);
}
