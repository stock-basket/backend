package com.hanyahunya.stockbasket.domain.stock.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.NotBlank;

import java.util.UUID;

/** 바구니 추가 요청: 종목 코드(stockCode) 로 추가 */
public record StockAddRequest(
        @JsonIgnore UUID userId,
        @NotBlank String stockCode
) {}
