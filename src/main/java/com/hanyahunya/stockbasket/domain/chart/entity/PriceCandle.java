package com.hanyahunya.stockbasket.domain.chart.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "price_candles",
        indexes = @Index(name = "idx_candle_code_time", columnList = "stock_code, minute_at"))
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class PriceCandle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "stock_code", nullable = false, length = 20)
    private String stockCode;

    /** KST 기준, 분 단위로 truncate */
    @Column(name = "minute_at", nullable = false)
    private LocalDateTime minuteAt;

    @Column(nullable = false) private long open;
    @Column(nullable = false) private long high;
    @Column(nullable = false) private long low;
    @Column(nullable = false) private long close;
}
