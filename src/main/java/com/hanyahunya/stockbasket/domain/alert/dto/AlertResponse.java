package com.hanyahunya.stockbasket.domain.alert.dto;
import lombok.Builder;
import lombok.Getter;
import java.time.LocalDateTime;

@Getter @Builder
public class AlertResponse {
    private Long id;
    private String stockName;
    private String alertType;
    private String message;
    private Double priceChangeRate;
    private boolean isRead;
    private LocalDateTime createdAt;
}
