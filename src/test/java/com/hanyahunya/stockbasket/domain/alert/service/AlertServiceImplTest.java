package com.hanyahunya.stockbasket.domain.alert.service;

import com.hanyahunya.stockbasket.domain.alert.dto.AlertResponse;
import com.hanyahunya.stockbasket.domain.alert.entity.Alert;
import com.hanyahunya.stockbasket.domain.alert.entity.AlertType;
import com.hanyahunya.stockbasket.domain.alert.repository.AlertRepository;
import com.hanyahunya.stockbasket.domain.alert.trigger.PriceTrigger;
import com.hanyahunya.stockbasket.domain.alert.trigger.TriggerResult;
import com.hanyahunya.stockbasket.domain.mail.service.MailService;
import com.hanyahunya.stockbasket.domain.stock.entity.MarketType;
import com.hanyahunya.stockbasket.domain.stock.entity.Stock;
import com.hanyahunya.stockbasket.domain.stock.repository.StockRepository;
import com.hanyahunya.stockbasket.domain.user.entity.Role;
import com.hanyahunya.stockbasket.domain.user.entity.User;
import com.hanyahunya.stockbasket.domain.user.entity.UserSetting;
import com.hanyahunya.stockbasket.domain.user.repository.UserRepository;
import com.hanyahunya.stockbasket.domain.user.repository.UserSettingRepository;
import com.hanyahunya.stockbasket.infra.kiwoom.PriceStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.ByteBuffer;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AlertServiceImplTest {

    @Mock AlertRepository alertRepository;
    @Mock StockRepository stockRepository;
    @Mock UserRepository userRepository;
    @Mock UserSettingRepository userSettingRepository;
    @Mock PriceStore priceStore;
    @Mock MailService mailService;

    // triggers 는 List 형태로 직접 구성
    @Mock PriceTrigger priceTrigger;

    AlertServiceImpl alertService;

    @BeforeEach
    void setUp() {
        alertService = new AlertServiceImpl(
                alertRepository, stockRepository, userRepository,
                userSettingRepository, priceStore, List.of(priceTrigger), mailService
        );
    }

    // ── getMyAlerts ────────────────────────────────────────────────────────────

    @Test
    void getMyAlerts_전체_알림_반환() {
        UUID userId = UUID.randomUUID();
        Stock stock = buildStock("005930", "삼성전자");
        User user = buildUser(userId);
        Alert a1 = buildAlert(1L, user, stock, AlertType.PRICE_SPIKE, false);
        Alert a2 = buildAlert(2L, user, stock, AlertType.PRICE_DROP, true);
        when(alertRepository.findAllByUserIdOrderByCreatedAtDesc(userId)).thenReturn(List.of(a1, a2));

        List<AlertResponse> result = alertService.getMyAlerts(userId);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).id()).isEqualTo(1L);
        assertThat(result.get(1).id()).isEqualTo(2L);
    }

    @Test
    void getMyAlerts_빈리스트_반환() {
        UUID userId = UUID.randomUUID();
        when(alertRepository.findAllByUserIdOrderByCreatedAtDesc(userId)).thenReturn(List.of());

        assertThat(alertService.getMyAlerts(userId)).isEmpty();
    }

    @Test
    void getMyAlerts_응답_필드_매핑_검증() {
        UUID userId = UUID.randomUUID();
        Stock stock = buildStock("005930", "삼성전자");
        User user = buildUser(userId);
        Alert alert = buildAlert(1L, user, stock, AlertType.PRICE_SPIKE, false);
        when(alertRepository.findAllByUserIdOrderByCreatedAtDesc(userId)).thenReturn(List.of(alert));

        List<AlertResponse> result = alertService.getMyAlerts(userId);

        AlertResponse response = result.get(0);
        assertThat(response.stockName()).isEqualTo("삼성전자");
        assertThat(response.alertType()).isEqualTo(AlertType.PRICE_SPIKE);
        assertThat(response.isRead()).isFalse();
    }

    // ── getUnreadAlerts ────────────────────────────────────────────────────────

    @Test
    void getUnreadAlerts_미읽음_알림만_반환() {
        UUID userId = UUID.randomUUID();
        Stock stock = buildStock("005930", "삼성전자");
        User user = buildUser(userId);
        Alert unread = buildAlert(1L, user, stock, AlertType.PRICE_SPIKE, false);
        when(alertRepository.findAllByUserIdAndReadIsFalseOrderByCreatedAtDesc(userId))
                .thenReturn(List.of(unread));

        List<AlertResponse> result = alertService.getUnreadAlerts(userId);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).isRead()).isFalse();
    }

    @Test
    void getUnreadAlerts_빈리스트_반환() {
        UUID userId = UUID.randomUUID();
        when(alertRepository.findAllByUserIdAndReadIsFalseOrderByCreatedAtDesc(userId))
                .thenReturn(List.of());

        assertThat(alertService.getUnreadAlerts(userId)).isEmpty();
    }

    // ── getUnreadCount ─────────────────────────────────────────────────────────

    @Test
    void getUnreadCount_개수_반환() {
        UUID userId = UUID.randomUUID();
        when(alertRepository.countByUserIdAndReadIsFalse(userId)).thenReturn(5);

        assertThat(alertService.getUnreadCount(userId)).isEqualTo(5);
    }

    @Test
    void getUnreadCount_0_반환() {
        UUID userId = UUID.randomUUID();
        when(alertRepository.countByUserIdAndReadIsFalse(userId)).thenReturn(0);

        assertThat(alertService.getUnreadCount(userId)).isZero();
    }

    // ── markAsRead ─────────────────────────────────────────────────────────────

    @Test
    void markAsRead_읽음_처리() {
        Stock stock = buildStock("005930", "삼성전자");
        User user = buildUser(UUID.randomUUID());
        Alert alert = buildAlert(1L, user, stock, AlertType.PRICE_SPIKE, false);
        when(alertRepository.findById(1L)).thenReturn(Optional.of(alert));

        alertService.markAsRead(1L);

        assertThat(alert.isRead()).isTrue();
    }

    @Test
    void markAsRead_미존재_알림_아무것도_안함() {
        when(alertRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatNoException().isThrownBy(() -> alertService.markAsRead(999L));
    }

    // ── markAllAsRead ──────────────────────────────────────────────────────────

    @Test
    void markAllAsRead_모든_미읽음_읽음처리() {
        UUID userId = UUID.randomUUID();
        Stock stock = buildStock("005930", "삼성전자");
        User user = buildUser(userId);
        Alert a1 = buildAlert(1L, user, stock, AlertType.PRICE_SPIKE, false);
        Alert a2 = buildAlert(2L, user, stock, AlertType.PRICE_DROP, false);
        when(alertRepository.findAllByUserIdAndReadIsFalseOrderByCreatedAtDesc(userId))
                .thenReturn(List.of(a1, a2));

        alertService.markAllAsRead(userId);

        assertThat(a1.isRead()).isTrue();
        assertThat(a2.isRead()).isTrue();
    }

    @Test
    void markAllAsRead_미읽음_없으면_아무것도_안함() {
        UUID userId = UUID.randomUUID();
        when(alertRepository.findAllByUserIdAndReadIsFalseOrderByCreatedAtDesc(userId))
                .thenReturn(List.of());

        assertThatNoException().isThrownBy(() -> alertService.markAllAsRead(userId));
    }

    // ── detectAndCreatePriceAlerts ─────────────────────────────────────────────

    @Test
    void detectAndCreatePriceAlerts_추적종목_없으면_저장_안함() {
        when(priceStore.getTrackedStockCodes()).thenReturn(Set.of());

        alertService.detectAndCreatePriceAlerts();

        verify(alertRepository, never()).save(any());
    }

    @Test
    void detectAndCreatePriceAlerts_현재가_없으면_스킵() {
        when(priceStore.getTrackedStockCodes()).thenReturn(Set.of("005930"));
        when(priceStore.getCurrentPrice("005930")).thenReturn(Optional.empty());

        alertService.detectAndCreatePriceAlerts();

        verify(alertRepository, never()).save(any());
    }

    @Test
    void detectAndCreatePriceAlerts_기준가_없으면_스킵() {
        when(priceStore.getTrackedStockCodes()).thenReturn(Set.of("005930"));
        when(priceStore.getCurrentPrice("005930")).thenReturn(Optional.of(105L));
        when(priceStore.getReferencePrice("005930")).thenReturn(Optional.empty());

        alertService.detectAndCreatePriceAlerts();

        verify(alertRepository, never()).save(any());
    }

    @Test
    void detectAndCreatePriceAlerts_현재가_0이면_스킵() {
        when(priceStore.getTrackedStockCodes()).thenReturn(Set.of("005930"));
        when(priceStore.getCurrentPrice("005930")).thenReturn(Optional.of(0L));
        when(priceStore.getReferencePrice("005930")).thenReturn(Optional.of(100L));

        alertService.detectAndCreatePriceAlerts();

        verify(alertRepository, never()).save(any());
    }

    @Test
    void detectAndCreatePriceAlerts_사용자없으면_저장_안함() {
        when(priceStore.getTrackedStockCodes()).thenReturn(Set.of("005930"));
        when(priceStore.getCurrentPrice("005930")).thenReturn(Optional.of(106L));
        when(priceStore.getReferencePrice("005930")).thenReturn(Optional.of(100L));
        when(stockRepository.findUserIdsByStockCode("005930")).thenReturn(List.of());

        alertService.detectAndCreatePriceAlerts();

        verify(alertRepository, never()).save(any());
    }

    @Test
    void detectAndCreatePriceAlerts_트리거_발동시_알림_저장() {
        UUID userId = UUID.randomUUID();
        String stockCode = "005930";

        when(priceStore.getTrackedStockCodes()).thenReturn(Set.of(stockCode));
        when(priceStore.getCurrentPrice(stockCode)).thenReturn(Optional.of(106L));
        when(priceStore.getReferencePrice(stockCode)).thenReturn(Optional.of(100L));
        when(stockRepository.findUserIdsByStockCode(stockCode)).thenReturn(List.of(toBytes(userId)));

        UserSetting setting = buildUserSetting(false);
        when(userSettingRepository.findByUser_Id(userId)).thenReturn(setting);
        when(priceTrigger.evaluate(eq(stockCode), eq(106L), eq(100L), eq(setting)))
                .thenReturn(Optional.of(new TriggerResult(AlertType.PRICE_SPIKE, 6.0)));
        when(priceStore.isCooldownActive(eq(userId), eq(stockCode), eq(AlertType.PRICE_SPIKE), any()))
                .thenReturn(false);

        Stock stock = buildStock(stockCode, "삼성전자");
        User user = buildUser(userId);
        when(stockRepository.findById(stockCode)).thenReturn(Optional.of(stock));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        alertService.detectAndCreatePriceAlerts();

        verify(alertRepository).save(any(Alert.class));
        verify(priceStore).recordAlert(userId, stockCode, AlertType.PRICE_SPIKE);
    }

    @Test
    void detectAndCreatePriceAlerts_이메일알림_활성화시_메일발송() {
        UUID userId = UUID.randomUUID();
        String stockCode = "005930";

        when(priceStore.getTrackedStockCodes()).thenReturn(Set.of(stockCode));
        when(priceStore.getCurrentPrice(stockCode)).thenReturn(Optional.of(106L));
        when(priceStore.getReferencePrice(stockCode)).thenReturn(Optional.of(100L));
        when(stockRepository.findUserIdsByStockCode(stockCode)).thenReturn(List.of(toBytes(userId)));

        UserSetting setting = buildUserSetting(true); // 이메일 활성화
        when(userSettingRepository.findByUser_Id(userId)).thenReturn(setting);
        when(priceTrigger.evaluate(any(), anyLong(), anyLong(), any()))
                .thenReturn(Optional.of(new TriggerResult(AlertType.PRICE_SPIKE, 6.0)));
        when(priceStore.isCooldownActive(any(), any(), any(), any())).thenReturn(false);

        Stock stock = buildStock(stockCode, "삼성전자");
        User user = buildUser(userId);
        when(stockRepository.findById(stockCode)).thenReturn(Optional.of(stock));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        alertService.detectAndCreatePriceAlerts();

        verify(mailService).sendTemplate(anyList(), anyString(), eq("alert/price-volatility"), anyMap());
    }

    @Test
    void detectAndCreatePriceAlerts_쿨다운_활성시_알림_저장_안함() {
        UUID userId = UUID.randomUUID();
        String stockCode = "005930";

        when(priceStore.getTrackedStockCodes()).thenReturn(Set.of(stockCode));
        when(priceStore.getCurrentPrice(stockCode)).thenReturn(Optional.of(106L));
        when(priceStore.getReferencePrice(stockCode)).thenReturn(Optional.of(100L));
        when(stockRepository.findUserIdsByStockCode(stockCode)).thenReturn(List.of(toBytes(userId)));

        UserSetting setting = buildUserSetting(false);
        when(userSettingRepository.findByUser_Id(userId)).thenReturn(setting);
        when(priceTrigger.evaluate(any(), anyLong(), anyLong(), any()))
                .thenReturn(Optional.of(new TriggerResult(AlertType.PRICE_SPIKE, 6.0)));
        when(priceStore.isCooldownActive(eq(userId), eq(stockCode), eq(AlertType.PRICE_SPIKE), any()))
                .thenReturn(true);

        alertService.detectAndCreatePriceAlerts();

        verify(alertRepository, never()).save(any());
    }

    @Test
    void detectAndCreatePriceAlerts_트리거_미발동시_저장_안함() {
        UUID userId = UUID.randomUUID();
        String stockCode = "005930";

        when(priceStore.getTrackedStockCodes()).thenReturn(Set.of(stockCode));
        when(priceStore.getCurrentPrice(stockCode)).thenReturn(Optional.of(102L));
        when(priceStore.getReferencePrice(stockCode)).thenReturn(Optional.of(100L));
        when(stockRepository.findUserIdsByStockCode(stockCode)).thenReturn(List.of(toBytes(userId)));

        UserSetting setting = buildUserSetting(false);
        when(userSettingRepository.findByUser_Id(userId)).thenReturn(setting);
        when(priceTrigger.evaluate(any(), anyLong(), anyLong(), any()))
                .thenReturn(Optional.empty());

        alertService.detectAndCreatePriceAlerts();

        verify(alertRepository, never()).save(any());
    }

    // ── helpers ────────────────────────────────────────────────────────────────

    private Alert buildAlert(Long id, User user, Stock stock, AlertType type, boolean read) {
        Alert alert = Alert.builder()
                .user(user).stock(stock).alertType(type)
                .message("테스트 알림").priceChangeRate(5.0)
                .build();
        ReflectionTestUtils.setField(alert, "id", id);
        ReflectionTestUtils.setField(alert, "createdAt", LocalDateTime.now());
        if (read) {
            alert.markRead();
        }
        return alert;
    }

    private Stock buildStock(String code, String name) {
        return Stock.builder().stockCode(code).name(name).market(MarketType.KOSPI).build();
    }

    private User buildUser(UUID id) {
        return User.builder()
                .id(id).email("test@example.com").password("encoded")
                .nickname("tester").role(Role.ROLE_USER).build();
    }

    private UserSetting buildUserSetting(boolean emailEnabled) {
        return UserSetting.builder()
                .isGlobalAlertEnabled(true)
                .isVolatilityAlertEnabled(true)
                .volatilityThresholdPercent(5.0)
                .isEmailAlertEnabled(emailEnabled)
                .build();
    }

    private byte[] toBytes(UUID uuid) {
        ByteBuffer bb = ByteBuffer.allocate(16);
        bb.putLong(uuid.getMostSignificantBits());
        bb.putLong(uuid.getLeastSignificantBits());
        return bb.array();
    }
}
