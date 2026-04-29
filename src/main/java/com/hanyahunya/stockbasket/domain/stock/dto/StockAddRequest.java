package com.hanyahunya.stockbasket.domain.stock.dto;

import jakarta.validation.constraints.NotBlank;

/** 바구니 추가 요청: 종목 코드(ticker) 로 추가 */
public record StockAddRequest(
        @NotBlank String ticker
) {}
