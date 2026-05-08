package com.hanyahunya.stockbasket;

import com.hanyahunya.stockbasket.infra.kiwoom.KiwoomTokenManager;
import com.hanyahunya.stockbasket.infra.kiwoom.KiwoomWebSocketClient;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

/**
 * Spring ApplicationContext 로딩 통합 테스트.
 *
 * 외부 연결이 필요한 빈들은 @MockitoBean 으로 대체:
 *  - KiwoomTokenManager  : @PostConstruct 에서 Kiwoom HTTP 토큰 발급 시도
 *  - KiwoomWebSocketClient : ApplicationReadyEvent 에서 WebSocket 연결 시도
 *  - JavaMailSender      : SMTP 서버 연결 시도
 *
 * DB 는 src/test/resources/application.yml 의 H2 인메모리 설정을 사용.
 */
@SpringBootTest
class StockbasketApplicationTests {

    @MockitoBean
    KiwoomTokenManager kiwoomTokenManager;

    @MockitoBean
    KiwoomWebSocketClient kiwoomWebSocketClient;

    @MockitoBean
    JavaMailSender javaMailSender;

    @Test
    void contextLoads() {
    }
}
