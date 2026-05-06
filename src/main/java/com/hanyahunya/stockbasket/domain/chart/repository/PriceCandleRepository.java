package com.hanyahunya.stockbasket.domain.chart.repository;

import com.hanyahunya.stockbasket.domain.chart.entity.PriceCandle;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface PriceCandleRepository extends JpaRepository<PriceCandle, Long> {

    List<PriceCandle> findByStockCodeAndMinuteAtAfterOrderByMinuteAtAsc(String stockCode, LocalDateTime after);
}
