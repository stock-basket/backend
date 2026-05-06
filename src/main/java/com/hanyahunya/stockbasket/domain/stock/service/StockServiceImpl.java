package com.hanyahunya.stockbasket.domain.stock.service;

import com.hanyahunya.stockbasket.domain.stock.StockErrorCode;
import com.hanyahunya.stockbasket.domain.stock.dto.StockAddRequest;
import com.hanyahunya.stockbasket.domain.stock.dto.StockResponse;
import com.hanyahunya.stockbasket.domain.stock.entity.Stock;
import com.hanyahunya.stockbasket.domain.stock.repository.StockRepository;
import com.hanyahunya.stockbasket.domain.user.entity.User;
import com.hanyahunya.stockbasket.domain.user.repository.UserRepository;
import com.hanyahunya.stockbasket.global.exception.BusinessException;
import com.hanyahunya.stockbasket.infra.kiwoom.KiwoomWebSocketClient;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class StockServiceImpl implements StockService {

    private static final int BASKET_LIMIT = 20;

    private final StockRepository stockRepository;
    private final UserRepository userRepository;

    // 순환 의존성 방지: KiwoomWebSocketClient → StockRepository, StockServiceImpl → KiwoomWebSocketClient
    @Lazy
    @Autowired
    private KiwoomWebSocketClient kiwoomWebSocketClient;

    @Override
    @Transactional
    public void addToBasket(UUID userId, StockAddRequest request) {
        Stock stock = stockRepository.findById(request.stockCode())
                .orElseThrow(() -> new BusinessException(StockErrorCode.STOCK_NOT_FOUND));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(StockErrorCode.STOCK_NOT_FOUND));

        if (user.getStocks().containsKey(stock.getStockCode())) {
            throw new BusinessException(StockErrorCode.STOCK_ALREADY_IN_BASKET);
        }
        if (user.getStocks().size() >= BASKET_LIMIT) {
            throw new BusinessException(StockErrorCode.BASKET_LIMIT_EXCEEDED);
        }

        user.getStocks().put(stock.getStockCode(), stock);
        kiwoomWebSocketClient.ensureSubscribed(stock.getStockCode(), userId);
    }

    @Override
    @Transactional
    public void removeFromBasket(UUID userId, String stockCode) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(StockErrorCode.STOCK_NOT_FOUND));

        if (!user.getStocks().containsKey(stockCode)) {
            throw new BusinessException(StockErrorCode.STOCK_NOT_FOUND);
        }
        user.getStocks().remove(stockCode);
    }

    @Override
    @Transactional(readOnly = true)
    public List<StockResponse> getMyBasket(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(StockErrorCode.STOCK_NOT_FOUND));

        return user.getStocks().values().stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public StockResponse getStockDetail(UUID userId, String stockCode) {
        Stock stock = stockRepository.findById(stockCode)
                .orElseThrow(() -> new BusinessException(StockErrorCode.STOCK_NOT_FOUND));
        return toResponse(stock);
    }

    @Override
    @Transactional(readOnly = true)
    public List<StockResponse> searchStocks(String keyword) {
        return stockRepository
                .findByNameContainingIgnoreCaseOrStockCodeContainingIgnoreCase(keyword, keyword)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private StockResponse toResponse(Stock stock) {
        return new StockResponse(
                stock.getStockCode(),
                stock.getName(),
                stock.getMarket().name(),
                0, 0, 0
        );
    }
}
