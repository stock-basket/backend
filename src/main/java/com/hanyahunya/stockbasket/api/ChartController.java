package com.hanyahunya.stockbasket.api;

import com.hanyahunya.stockbasket.infra.chart.ChartSseRegistry;
import com.hanyahunya.stockbasket.infra.kiwoom.KiwoomWebSocketClient;
import com.hanyahunya.stockbasket.infra.kiwoom.PriceStore;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.time.Instant;

@RestController
@RequestMapping("/api/chart")
@RequiredArgsConstructor
public class ChartController {

    private final KiwoomWebSocketClient kiwoomWebSocketClient;
    private final ChartSseRegistry sseRegistry;
    private final PriceStore priceStore;

    /**
     * 실시간 가격 tick SSE 스트림.
     * 해당 종목이 Kiwoom에서 수신 중이 아니면 구독을 새로 시작한다.
     * 접속 시점의 현재가를 첫 이벤트로 즉시 전송하고,
     * 이후 Kiwoom tick 마다 한 번씩 전송한다.
     */
    @GetMapping(value = "/{stockCode}/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream(@PathVariable String stockCode) {
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);

        kiwoomWebSocketClient.subscribeForChart(stockCode);
        sseRegistry.register(stockCode, emitter);

        // 접속 시점의 현재가를 즉시 전송 (이전 데이터 없으면 생략)
        priceStore.getCurrentPrice(stockCode).ifPresent(price -> {
            try {
                String json = "{\"stockCode\":\"%s\",\"price\":%d,\"time\":\"%s\"}"
                        .formatted(stockCode, price, Instant.now());
                emitter.send(SseEmitter.event().name("snapshot").data(json));
            } catch (Exception ignored) {
            }
        });

        return emitter;
    }
}
