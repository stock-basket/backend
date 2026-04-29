package com.hanyahunya.stockbasket.domain.stock.service;
import com.hanyahunya.stockbasket.domain.stock.dto.StockAddRequest;
import com.hanyahunya.stockbasket.domain.stock.dto.StockResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class StockServiceImpl implements StockService {

    @Override
    public void addToBasket(UUID userId, StockAddRequest request) {

    }

    @Override
    public void removeFromBasket(UUID userId, String stockCode) {

    }

    @Override
    public List<StockResponse> getMyBasket(UUID userId) {
        return List.of();
    }

    @Override
    public StockResponse getStockDetail(UUID userId, String stockCode) {
        return null;
    }

    @Override
    public List<StockResponse> searchStocks(String keyword) {
        return List.of();
    }
}
