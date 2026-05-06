package com.hanyahunya.stockbasket.infra.chart;

import com.hanyahunya.stockbasket.domain.chart.entity.PriceCandle;
import com.hanyahunya.stockbasket.domain.chart.repository.PriceCandleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class CandleAggregator {

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    private final PriceCandleRepository candleRepository;

    private final ConcurrentHashMap<String, CandleBuilder> builders = new ConcurrentHashMap<>();

    /** Kiwoom tick 수신 시 호출 */
    public void onTick(String stockCode, long price) {
        LocalDateTime minuteAt = LocalDateTime.now(KST).truncatedTo(ChronoUnit.MINUTES);

        CandleBuilder current = builders.get(stockCode);

        if (current == null) {
            builders.putIfAbsent(stockCode, new CandleBuilder(stockCode, minuteAt, price));
            return;
        }

        if (!current.minuteAt.equals(minuteAt)) {
            // 분이 바뀌었으면 이전 캔들 저장 후 새 캔들 시작
            PriceCandle completed = current.build();
            builders.put(stockCode, new CandleBuilder(stockCode, minuteAt, price));
            candleRepository.save(completed);
            log.debug("[Candle] {} {} O:{} H:{} L:{} C:{}",
                    stockCode, current.minuteAt,
                    completed.getOpen(), completed.getHigh(),
                    completed.getLow(), completed.getClose());
        } else {
            current.addTick(price);
        }
    }

    private static class CandleBuilder {
        final String stockCode;
        final LocalDateTime minuteAt;
        long open, high, low, close;

        CandleBuilder(String stockCode, LocalDateTime minuteAt, long firstPrice) {
            this.stockCode = stockCode;
            this.minuteAt  = minuteAt;
            this.open = this.high = this.low = this.close = firstPrice;
        }

        void addTick(long price) {
            if (price > high) high = price;
            if (price < low)  low  = price;
            close = price;
        }

        PriceCandle build() {
            return PriceCandle.builder()
                    .stockCode(stockCode)
                    .minuteAt(minuteAt)
                    .open(open).high(high).low(low).close(close)
                    .build();
        }
    }
}
