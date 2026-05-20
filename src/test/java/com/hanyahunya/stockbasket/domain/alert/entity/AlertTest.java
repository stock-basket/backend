package com.hanyahunya.stockbasket.domain.alert.entity;

import com.hanyahunya.stockbasket.domain.stock.entity.MarketType;
import com.hanyahunya.stockbasket.domain.stock.entity.Stock;
import com.hanyahunya.stockbasket.domain.user.entity.Role;
import com.hanyahunya.stockbasket.domain.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

class AlertTest {

    private User user;
    private Stock stock;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(UUID.randomUUID()).email("test@example.com")
                .password("encoded").nickname("tester").role(Role.ROLE_USER)
                .build();
        stock = Stock.builder()
                .stockCode("005930").name("삼성전자").market(MarketType.KOSPI).build();
    }

    @Test
    void markRead_읽음_처리() {
        Alert alert = Alert.builder()
                .user(user).stock(stock)
                .alertType(AlertType.PRICE_SPIKE)
                .message("급등 알림").priceChangeRate(6.0)
                .build();
        ReflectionTestUtils.setField(alert, "isRead", false);

        alert.markRead();

        assertThat(alert.isRead()).isTrue();
    }

    @Test
    void markRead_이미_읽음_상태에서_재호출_idempotent() {
        Alert alert = Alert.builder()
                .user(user).stock(stock)
                .alertType(AlertType.PRICE_DROP)
                .message("급락 알림").priceChangeRate(-5.0)
                .build();
        ReflectionTestUtils.setField(alert, "isRead", true);

        alert.markRead();

        assertThat(alert.isRead()).isTrue();
    }

    @Test
    void builder_모든_AlertType_생성() {
        for (AlertType type : AlertType.values()) {
            Alert alert = Alert.builder()
                    .user(user).stock(stock)
                    .alertType(type)
                    .message("알림").priceChangeRate(5.0)
                    .build();
            assertThat(alert.getAlertType()).isEqualTo(type);
        }
    }

    @Test
    void alert_기본_isRead_false() {
        Alert alert = Alert.builder()
                .user(user).stock(stock)
                .alertType(AlertType.PRICE_SPIKE)
                .message("테스트").priceChangeRate(5.0)
                .build();
        ReflectionTestUtils.setField(alert, "isRead", false);

        assertThat(alert.isRead()).isFalse();
    }
}
