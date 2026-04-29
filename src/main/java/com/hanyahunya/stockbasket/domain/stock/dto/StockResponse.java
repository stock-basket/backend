package com.hanyahunya.stockbasket.domain.stock.dto;

/**
 * 종목 응답 DTO.
 * 바구니 목록, 종목 상세, 검색 결과에 공통 사용.
 */
public record StockResponse(
        String stockCode,
        String ticker,
        String name,
        String market,           // "KOSPI" | "KOSDAQ"
        int    positiveNewsCount,
        int    negativeNewsCount,
        int    neutralNewsCount
) {}
