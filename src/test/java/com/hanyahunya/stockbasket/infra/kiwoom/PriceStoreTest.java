package com.hanyahunya.stockbasket.infra.kiwoom;

import com.hanyahunya.stockbasket.domain.alert.entity.AlertType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

class PriceStoreTest {

    private PriceStore priceStore;

    @BeforeEach
    void setUp() {
        priceStore = new PriceStore();
    }

    @Test
    void updateCurrentPrice_현재가_저장() {
        priceStore.updateCurrentPrice("005930", 75_000L);

        assertThat(priceStore.getCurrentPrice("005930"))
                .isPresent()
                .hasValue(75_000L);
    }

    @Test
    void updateCurrentPrice_기준가_최초_자동설정() {
        priceStore.updateCurrentPrice("005930", 75_000L);

        assertThat(priceStore.getReferencePrice("005930"))
                .isPresent()
                .hasValue(75_000L);
    }

    @Test
    void updateCurrentPrice_기준가_이미있으면_덮어쓰지_않음() {
        priceStore.setReferencePrice("005930", 70_000L);
        priceStore.updateCurrentPrice("005930", 75_000L);

        assertThat(priceStore.getReferencePrice("005930")).hasValue(70_000L);
    }

    @Test
    void setReferencePrice_기준가_명시_설정() {
        priceStore.setReferencePrice("005930", 68_000L);

        assertThat(priceStore.getReferencePrice("005930"))
                .isPresent()
                .hasValue(68_000L);
    }

    @Test
    void getCurrentPrice_미등록_종목_empty() {
        assertThat(priceStore.getCurrentPrice("999999")).isEmpty();
    }

    @Test
    void getReferencePrice_미등록_종목_empty() {
        assertThat(priceStore.getReferencePrice("999999")).isEmpty();
    }

    @Test
    void snapshotCurrentAsReference_현재가를_기준가로_복사() {
        priceStore.setReferencePrice("005930", 70_000L);
        priceStore.updateCurrentPrice("005930", 80_000L);

        priceStore.snapshotCurrentAsReference();

        assertThat(priceStore.getReferencePrice("005930")).hasValue(80_000L);
    }

    @Test
    void getTrackedStockCodes_등록된_모든_종목_반환() {
        priceStore.updateCurrentPrice("005930", 75_000L);
        priceStore.updateCurrentPrice("035720", 60_000L);
        priceStore.updateCurrentPrice("000660", 130_000L);

        assertThat(priceStore.getTrackedStockCodes())
                .containsExactlyInAnyOrder("005930", "035720", "000660");
    }

    @Test
    void initStock_현재가_0으로_슬롯_생성() {
        priceStore.initStock("005930");

        assertThat(priceStore.getCurrentPrice("005930"))
                .isPresent()
                .hasValue(0L);
    }

    @Test
    void initStock_이미_존재하면_덮어쓰지_않음() {
        priceStore.updateCurrentPrice("005930", 75_000L);
        priceStore.initStock("005930");

        assertThat(priceStore.getCurrentPrice("005930")).hasValue(75_000L);
    }

    @Test
    void isCooldownActive_쿨다운_미설정시_false() {
        UUID userId = UUID.randomUUID();

        boolean result = priceStore.isCooldownActive(userId, "005930", AlertType.PRICE_SPIKE, Duration.ofMinutes(30));

        assertThat(result).isFalse();
    }

    @Test
    void recordAlert_후_isCooldownActive_true() {
        UUID userId = UUID.randomUUID();
        priceStore.recordAlert(userId, "005930", AlertType.PRICE_SPIKE);

        boolean result = priceStore.isCooldownActive(userId, "005930", AlertType.PRICE_SPIKE, Duration.ofMinutes(30));

        assertThat(result).isTrue();
    }

    @Test
    void 다른_AlertType은_쿨다운_독립적_적용() {
        UUID userId = UUID.randomUUID();
        priceStore.recordAlert(userId, "005930", AlertType.PRICE_SPIKE);

        assertThat(priceStore.isCooldownActive(userId, "005930", AlertType.PRICE_DROP, Duration.ofMinutes(30))).isFalse();
        assertThat(priceStore.isCooldownActive(userId, "005930", AlertType.HIGH_IMPACT_NEWS, Duration.ofMinutes(30))).isFalse();
    }

    @Test
    void 다른_사용자는_쿨다운_독립적_적용() {
        UUID user1 = UUID.randomUUID();
        UUID user2 = UUID.randomUUID();
        priceStore.recordAlert(user1, "005930", AlertType.PRICE_SPIKE);

        assertThat(priceStore.isCooldownActive(user2, "005930", AlertType.PRICE_SPIKE, Duration.ofMinutes(30))).isFalse();
    }

    @Test
    void 다른_종목은_쿨다운_독립적_적용() {
        UUID userId = UUID.randomUUID();
        priceStore.recordAlert(userId, "005930", AlertType.PRICE_SPIKE);

        assertThat(priceStore.isCooldownActive(userId, "035720", AlertType.PRICE_SPIKE, Duration.ofMinutes(30))).isFalse();
    }

    @Test
    void isCooldownActive_쿨다운_만료시_false() throws InterruptedException {
        UUID userId = UUID.randomUUID();
        priceStore.recordAlert(userId, "005930", AlertType.PRICE_SPIKE);

        // 1ms 쿨다운은 이미 만료
        boolean result = priceStore.isCooldownActive(userId, "005930", AlertType.PRICE_SPIKE, Duration.ofMillis(1));

        // 바로 호출하면 아직 활성일 수 있으므로 약간의 여유를 줌
        Thread.sleep(5);
        boolean afterExpiry = priceStore.isCooldownActive(userId, "005930", AlertType.PRICE_SPIKE, Duration.ofMillis(1));
        assertThat(afterExpiry).isFalse();
    }
}
