package com.hanyahunya.stockbasket.infra.kiwoom;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import com.hanyahunya.stockbasket.global.exception.ExternalException;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;

@Slf4j
@Component
@RequiredArgsConstructor
public class KiwoomTokenManager {

    private final KiwoomProperties props;
    private final ObjectMapper objectMapper;

    private volatile String currentToken;
    private volatile Instant expiresAt = Instant.MIN;

    @PostConstruct
    public void init() {
        refresh();
    }

    @Scheduled(fixedRateString = "${kiwoom.token-refresh-interval-ms:21600000}",
               initialDelayString = "${kiwoom.token-refresh-interval-ms:21600000}")
    public void scheduledRefresh() {
        refresh();
    }

    public synchronized String getValidToken() {
        if (Instant.now().isAfter(expiresAt.minusSeconds(300))) {
            refresh();
        }
        return currentToken;
    }

    private void refresh() {
        try {
            String body = """
                    {"grant_type":"client_credentials","appkey":"%s","secretkey":"%s"}
                    """.formatted(props.appkey(), props.secretkey()).strip();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(props.tokenUrl()))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();

            HttpResponse<String> response = HttpClient.newHttpClient()
                    .send(request, HttpResponse.BodyHandlers.ofString());

            JsonNode node = objectMapper.readTree(response.body());
            currentToken = node.get("token").asText();
            long expiresIn = node.has("expires_in") ? node.get("expires_in").asLong() : 86400L;
            expiresAt = Instant.now().plusSeconds(expiresIn);

            log.info("[Kiwoom] 액세스 토큰 발급 완료 (만료까지 {}초)", expiresIn);
        } catch (Exception e) {
            log.error("[Kiwoom] 토큰 발급 실패: {}", e.getMessage());
            throw new ExternalException(KiwoomErrorCode.TOKEN_FETCH_FAILED, e);
        }
    }
}
