package com.hanyahunya.stockbasket.domain.stock.service;

import com.hanyahunya.stockbasket.domain.stock.dto.StockAddRequest;
import com.hanyahunya.stockbasket.domain.stock.dto.StockResponse;

import java.util.List;
import java.util.UUID;

public interface StockService {

    /**
     * 바구니에 종목 추가
     *
     * @throws com.hanyahunya.stockbasket.global.exception.BusinessException
     *         STOCK_NOT_FOUND(404), STOCK_ALREADY_IN_BASKET(409), BASKET_LIMIT_EXCEEDED(400)
     */
    void addToBasket(UUID userId, StockAddRequest request);

    /**
     * 바구니에서 종목 제거
     *
     * @param stockCode 종목 코드
     * @throws com.hanyahunya.stockbasket.global.exception.BusinessException
     *         STOCK_NOT_FOUND(404)
     */
    void removeFromBasket(UUID userId, String stockCode);

    /** 내 바구니 종목 목록 조회 */
    List<StockResponse> getMyBasket(UUID userId);

    /**
     * 종목 상세 조회
     *
     * @throws com.hanyahunya.stockbasket.global.exception.BusinessException
     *         STOCK_NOT_FOUND(404)
     */
    StockResponse getStockDetail(UUID userId, String stockCode);

    /** 종목명 또는 종목코드 키워드 검색 */
    List<StockResponse> searchStocks(String keyword);
}
