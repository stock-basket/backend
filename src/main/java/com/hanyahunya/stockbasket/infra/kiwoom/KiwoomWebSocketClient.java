package com.hanyahunya.stockbasket.infra.kiwoom;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import com.hanyahunya.stockbasket.domain.stock.repository.StockRepository;
import com.hanyahunya.stockbasket.domain.user.entity.UserSetting;
import com.hanyahunya.stockbasket.domain.user.repository.UserSettingRepository;
import com.hanyahunya.stockbasket.infra.chart.CandleAggregator;
import com.hanyahunya.stockbasket.infra.chart.ChartSseRegistry;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.nio.ByteBuffer;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Component
@RequiredArgsConstructor
public class KiwoomWebSocketClient implements ApplicationListener<ApplicationReadyEvent> {

    private static final int MAX_RECONNECT_DELAY_SEC = 60;
    private static final ZoneId KST = ZoneId.of("Asia/Seoul");
    private static final LocalTime MARKET_OPEN  = LocalTime.of(9, 0);
    private static final LocalTime MARKET_CLOSE = LocalTime.of(15, 20);

    private final KiwoomProperties props;
    private final KiwoomTokenManager tokenManager;
    private final PriceStore priceStore;
    private final StockRepository stockRepository;
    private final UserSettingRepository userSettingRepository;
    private final ObjectMapper objectMapper;
    private final ChartSseRegistry chartSseRegistry;
    private final CandleAggregator candleAggregator;

    private static final int PING_INTERVAL_SEC = 20;

    private volatile WebSocket webSocket;
    private final Set<String> subscribedCodes = java.util.concurrent.ConcurrentHashMap.newKeySet();
    private final AtomicInteger reconnectDelay = new AtomicInteger(3);
    private volatile ScheduledFuture<?> pingFuture;

    private final ScheduledExecutorService reconnectExecutor =
            Executors.newSingleThreadScheduledExecutor(r -> {
                Thread t = new Thread(r, "kiwoom-reconnect");
                t.setDaemon(true);
                return t;
            });

    private final ScheduledExecutorService pingExecutor =
            Executors.newSingleThreadScheduledExecutor(r -> {
                Thread t = new Thread(r, "kiwoom-ping");
                t.setDaemon(true);
                return t;
            });

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        if (isMarketHours()) {
            reconnectExecutor.submit(this::doConnect);
        } else {
            log.info("[Kiwoom] 장외 시간 — 웹소켓 연결 건너뜀");
        }
    }

    /** 장 시작 스케줄러에서 호출 */
    public void connect() {
        reconnectExecutor.submit(this::doConnect);
    }

    /** 장 종료 스케줄러에서 호출 */
    public void disconnect() {
        cancelPing();
        subscribedCodes.clear();
        WebSocket ws = webSocket;
        webSocket = null;
        if (ws != null && !ws.isInputClosed()) {
            ws.sendClose(WebSocket.NORMAL_CLOSURE, "market-close");
        }
        log.info("[Kiwoom] 장 종료 — 웹소켓 연결 해제");
    }

    private void doConnect() {
        if (!isMarketHours()) {
            log.info("[Kiwoom] 장외 시간 — 연결 건너뜀");
            return;
        }
        try {
            String token = tokenManager.getValidToken();

            webSocket = HttpClient.newHttpClient()
                    .newWebSocketBuilder()
                    .buildAsync(URI.create(props.wsUrl()), new MessageListener())
                    .join();

            log.info("[Kiwoom] CONNECTED");
            reconnectDelay.set(3);

            sendJson("{\"trnm\":\"LOGIN\",\"token\":\"" + token + "\"}");

        } catch (Exception e) {
            log.error("[Kiwoom] 연결 실패: {}", e.getMessage());
            scheduleReconnect();
        }
    }

    private void loadAndSubscribeAll() {
        List<String> dbCodes = stockRepository.findVolatilityEnabledStockCodes();
        dbCodes.forEach(priceStore::initStock);
        subscribedCodes.addAll(dbCodes);

        if (subscribedCodes.isEmpty()) {
            log.info("[Kiwoom] 구독할 종목 없음 — 구독 건너뜀");
            return;
        }
        List<String> all = new java.util.ArrayList<>(subscribedCodes);
        sendReg(all);
        log.info("[Kiwoom] SUBSCRIBED {} 종목 (DB:{}, 차트:{})",
                all.size(), dbCodes.size(), all.size() - dbCodes.size());
    }

    /** StockServiceImpl.addToBasket()에서 호출 — 신규 종목 실시간 구독 */
    public void ensureSubscribed(String stockCode, UUID userId) {
        if (!isMarketHours()) return;
        UserSetting setting = userSettingRepository.findByUser_Id(userId);
        if (setting == null || !setting.isVolatilityAlertEnabled()) return;
        if (subscribedCodes.add(stockCode)) {
            priceStore.initStock(stockCode);
            if (webSocket != null && !webSocket.isInputClosed()) {
                sendReg(List.of(stockCode));
                log.info("[Kiwoom] 구독 추가: {}", stockCode);
            }
        }
    }

    /** ChartController에서 호출 — 유저 설정 무관하게 차트용 구독 추가 */
    public void subscribeForChart(String stockCode) {
        if (!isMarketHours()) return;
        if (subscribedCodes.add(stockCode)) {
            priceStore.initStock(stockCode);
            if (webSocket != null && !webSocket.isInputClosed()) {
                sendReg(List.of(stockCode));
                log.info("[Kiwoom] 차트 구독 추가: {}", stockCode);
            }
        }
    }

    private boolean isMarketHours() {
        ZonedDateTime now = ZonedDateTime.now(KST);
        DayOfWeek dow = now.getDayOfWeek();
        if (dow == DayOfWeek.SATURDAY || dow == DayOfWeek.SUNDAY) return false;
        LocalTime time = now.toLocalTime();
        return !time.isBefore(MARKET_OPEN) && time.isBefore(MARKET_CLOSE);
    }

    private void sendReg(List<String> codes) {
        String itemArray = codes.stream()
                .map(c -> "\"" + c + "\"")
                .reduce((a, b) -> a + "," + b)
                .orElse("");
        sendJson("{\"trnm\":\"REG\",\"grp_no\":\"1\",\"data\":[{\"item\":[" + itemArray + "],\"type\":[\"0B\"]}]}");
    }

    private void sendJson(String json) {
        WebSocket ws = webSocket;
        if (ws != null && !ws.isInputClosed()) {
            ws.sendText(json, true);
        }
    }

    private void startPing() {
        cancelPing();
        pingFuture = pingExecutor.scheduleAtFixedRate(() -> {
            WebSocket ws = webSocket;
            if (ws != null && !ws.isInputClosed()) {
                ws.sendPing(ByteBuffer.allocate(0));
                log.debug("[Kiwoom] PING 전송");
            }
        }, PING_INTERVAL_SEC, PING_INTERVAL_SEC, TimeUnit.SECONDS);
    }

    private void cancelPing() {
        ScheduledFuture<?> f = pingFuture;
        if (f != null) {
            f.cancel(false);
            pingFuture = null;
        }
    }

    private void scheduleReconnect() {
        if (!isMarketHours()) {
            log.info("[Kiwoom] 장외 시간 — 재연결 건너뜀");
            return;
        }
        int delay = reconnectDelay.getAndUpdate(d -> Math.min(d * 2, MAX_RECONNECT_DELAY_SEC));
        log.warn("[Kiwoom] {}초 후 재연결 시도", delay);
        reconnectExecutor.schedule(this::doConnect, delay, TimeUnit.SECONDS);
    }

    @PreDestroy
    public void shutdown() {
        cancelPing();
        WebSocket ws = webSocket;
        if (ws != null) {
            ws.sendClose(WebSocket.NORMAL_CLOSURE, "shutdown");
        }
        reconnectExecutor.shutdownNow();
        pingExecutor.shutdownNow();
    }

    private class MessageListener implements WebSocket.Listener {

        private final StringBuilder buffer = new StringBuilder();

        @Override
        public CompletionStage<?> onText(WebSocket ws, CharSequence data, boolean last) {
            buffer.append(data);
            if (last) {
                String raw = buffer.toString();
                buffer.setLength(0);
                handleMessage(raw);
            }
            ws.request(1);
            return null;
        }

        @Override
        public CompletionStage<?> onPong(WebSocket ws, ByteBuffer message) {
            log.debug("[Kiwoom] PONG 수신");
            ws.request(1);
            return null;
        }

        @Override
        public CompletionStage<?> onClose(WebSocket ws, int statusCode, String reason) {
            log.warn("[Kiwoom] 연결 종료 (code={}, reason={})", statusCode, reason);
            scheduleReconnect();
            return null;
        }

        @Override
        public void onError(WebSocket ws, Throwable error) {
            log.error("[Kiwoom] WebSocket 오류: {}", error.getMessage());
            scheduleReconnect();
        }

        private void handleMessage(String raw) {
            try {
                JsonNode node = objectMapper.readTree(raw);
                String trnm = node.path("trnm").asText();

                if ("LOGIN".equals(trnm)) {
                    String returnCode = node.path("return_code").asText();
                    if ("0".equals(returnCode)) {
                        log.info("[Kiwoom] LOGIN OK");
                        startPing();
                        loadAndSubscribeAll();
                    } else {
                        log.error("[Kiwoom] LOGIN 거부: {}", raw);
                    }
                    return;
                }

                if ("REAL".equals(trnm)) {
                    log.debug("[Kiwoom] REAL raw: {}", raw);
                    JsonNode dataArr = node.path("data");
                    if (!dataArr.isArray() || dataArr.isEmpty()) {
                        log.warn("[Kiwoom] REAL data 배열 비어있음: {}", raw);
                        return;
                    }

                    JsonNode dataNode = dataArr.get(0);
                    String stockCode = dataNode.path("item").textValue();
                    String rawPrice  = dataNode.path("values").path("10").textValue();

                    log.info("[Kiwoom] REAL 수신 — stockCode={}, rawPrice={}", stockCode, rawPrice);

                    if (stockCode == null || rawPrice == null) {
                        log.warn("[Kiwoom] REAL 필드 누락 — stockCode={}, rawPrice={}", stockCode, rawPrice);
                        return;
                    }

                    // 현재가는 부호(+/-)가 붙을 수 있음 — 제거 후 파싱
                    long price = Long.parseLong(rawPrice.replaceAll("[^0-9]", ""));
                    if (price > 0) {
                        priceStore.updateCurrentPrice(stockCode, price);
                        chartSseRegistry.broadcast(stockCode, price);
                        candleAggregator.onTick(stockCode, price);
                    }
                }

            } catch (Exception e) {
                log.warn("[Kiwoom] 메시지 파싱 실패: {} / raw={}", e.getMessage(), raw);
            }
        }
    }
}
