package com.hanyahunya.stockbasket.infra.chart;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Slf4j
@Component
public class ChartSseRegistry {

    /** stockCode → 구독 중인 emitter 목록 */
    private final ConcurrentHashMap<String, CopyOnWriteArrayList<SseEmitter>> emitters =
            new ConcurrentHashMap<>();

    public void register(String stockCode, SseEmitter emitter) {
        CopyOnWriteArrayList<SseEmitter> list =
                emitters.computeIfAbsent(stockCode, k -> new CopyOnWriteArrayList<>());
        list.add(emitter);

        Runnable remove = () -> removeEmitter(stockCode, emitter);
        emitter.onCompletion(remove);
        emitter.onTimeout(remove);
        emitter.onError(e -> removeEmitter(stockCode, emitter));
    }

    /** Kiwoom tick 수신 시 해당 종목 구독자 전체에게 전송 */
    public void broadcast(String stockCode, long price) {
        CopyOnWriteArrayList<SseEmitter> list = emitters.get(stockCode);
        if (list == null || list.isEmpty()) {
            log.debug("[Chart SSE] broadcast 건너뜀 — 구독자 없음: {}", stockCode);
            return;
        }
        log.info("[Chart SSE] broadcast — stockCode={}, price={}, 구독자={}", stockCode, price, list.size());

        String json = "{\"stockCode\":\"%s\",\"price\":%d,\"time\":\"%s\"}"
                .formatted(stockCode, price, Instant.now());

        List<SseEmitter> dead = new ArrayList<>();
        for (SseEmitter emitter : list) {
            try {
                emitter.send(SseEmitter.event().name("tick").data(json));
            } catch (Exception e) {
                dead.add(emitter);
            }
        }
        dead.forEach(list::remove);
    }

    public boolean hasSubscribers(String stockCode) {
        CopyOnWriteArrayList<SseEmitter> list = emitters.get(stockCode);
        return list != null && !list.isEmpty();
    }

    private void removeEmitter(String stockCode, SseEmitter emitter) {
        CopyOnWriteArrayList<SseEmitter> list = emitters.get(stockCode);
        if (list != null) {
            list.remove(emitter);
            log.debug("[Chart SSE] 구독 해제: {} (남은 구독자: {})", stockCode, list.size());
        }
    }
}
