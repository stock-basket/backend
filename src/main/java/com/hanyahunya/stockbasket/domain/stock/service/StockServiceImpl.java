package com.hanyahunya.stockbasket.domain.stock.service;
import com.hanyahunya.stockbasket.domain.stock.dto.StockAddRequest;
import com.hanyahunya.stockbasket.domain.stock.dto.StockResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StockServiceImpl implements StockService {

    @Override
    public void addToBasket(Long userId, StockAddRequest request) {}

    @Override
    public void removeFromBasket(Long userId, Long stockId) {}

    @Override
    public List<StockResponse> getMyBasket(Long userId) { return List.of(); }

    @Override
    public StockResponse getStockDetail(Long userId, Long stockId) { return null; }

    @Override
    public List<StockResponse> searchStocks(String keyword) { return List.of(); }
}
