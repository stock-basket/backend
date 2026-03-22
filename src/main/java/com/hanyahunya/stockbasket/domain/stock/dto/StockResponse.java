package com.hanyahunya.stockbasket.domain.stock.dto;
import lombok.Builder;
import lombok.Getter;

@Getter @Builder
public class StockResponse {
    private Long id;
    private String ticker;
    private String name;
    private String market;
    private String sector;
    private int positiveNewsCount;
    private int negativeNewsCount;
    private int neutralNewsCount;
}
