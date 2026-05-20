package com.hanyahunya.stockbasket.domain.stock.service;

import com.hanyahunya.stockbasket.domain.stock.StockErrorCode;
import com.hanyahunya.stockbasket.domain.stock.dto.StockAddRequest;
import com.hanyahunya.stockbasket.domain.stock.dto.StockResponse;
import com.hanyahunya.stockbasket.domain.stock.entity.MarketType;
import com.hanyahunya.stockbasket.domain.stock.entity.Stock;
import com.hanyahunya.stockbasket.domain.stock.repository.StockRepository;
import com.hanyahunya.stockbasket.domain.user.entity.Role;
import com.hanyahunya.stockbasket.domain.user.entity.User;
import com.hanyahunya.stockbasket.domain.user.repository.UserRepository;
import com.hanyahunya.stockbasket.global.exception.BusinessException;
import com.hanyahunya.stockbasket.infra.kiwoom.KiwoomWebSocketClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StockServiceImplTest {

    @Mock StockRepository stockRepository;
    @Mock UserRepository userRepository;
    @Mock KiwoomWebSocketClient kiwoomWebSocketClient;

    @InjectMocks StockServiceImpl stockService;

    @BeforeEach
    void injectLazyDep() {
        ReflectionTestUtils.setField(stockService, "kiwoomWebSocketClient", kiwoomWebSocketClient);
    }

    // ── addToBasket ────────────────────────────────────────────────────────────

    @Test
    void addToBasket_정상_추가() {
        UUID userId = UUID.randomUUID();
        Stock stock = buildStock("005930", "삼성전자");
        User user = buildUser(userId);
        when(stockRepository.findById("005930")).thenReturn(Optional.of(stock));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        assertThatNoException().isThrownBy(() ->
                stockService.addToBasket(userId, new StockAddRequest(userId, "005930")));

        assertThat(user.getStocks()).containsKey("005930");
        verify(kiwoomWebSocketClient).ensureSubscribed("005930", userId);
    }

    @Test
    void addToBasket_미존재_종목_STOCK_NOT_FOUND_예외() {
        UUID userId = UUID.randomUUID();
        when(stockRepository.findById("000000")).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                stockService.addToBasket(userId, new StockAddRequest(userId, "000000")))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode())
                        .isEqualTo(StockErrorCode.STOCK_NOT_FOUND));

        verify(kiwoomWebSocketClient, never()).ensureSubscribed(any(), any());
    }

    @Test
    void addToBasket_이미_담긴_종목_STOCK_ALREADY_IN_BASKET_예외() {
        UUID userId = UUID.randomUUID();
        Stock stock = buildStock("005930", "삼성전자");
        User user = buildUser(userId);
        user.getStocks().put("005930", stock);
        when(stockRepository.findById("005930")).thenReturn(Optional.of(stock));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        assertThatThrownBy(() ->
                stockService.addToBasket(userId, new StockAddRequest(userId, "005930")))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode())
                        .isEqualTo(StockErrorCode.STOCK_ALREADY_IN_BASKET));
    }

    @Test
    void addToBasket_바구니_한도_초과_BASKET_LIMIT_EXCEEDED_예외() {
        UUID userId = UUID.randomUUID();
        Stock newStock = buildStock("999999", "신규종목");
        User user = buildUser(userId);
        // 바구니에 20개 채우기
        for (int i = 0; i < 20; i++) {
            Stock s = buildStock(String.format("%06d", i), "종목" + i);
            user.getStocks().put(s.getStockCode(), s);
        }
        when(stockRepository.findById("999999")).thenReturn(Optional.of(newStock));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        assertThatThrownBy(() ->
                stockService.addToBasket(userId, new StockAddRequest(userId, "999999")))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode())
                        .isEqualTo(StockErrorCode.BASKET_LIMIT_EXCEEDED));
    }

    // ── removeFromBasket ───────────────────────────────────────────────────────

    @Test
    void removeFromBasket_정상_제거() {
        UUID userId = UUID.randomUUID();
        Stock stock = buildStock("005930", "삼성전자");
        User user = buildUser(userId);
        user.getStocks().put("005930", stock);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        assertThatNoException().isThrownBy(() ->
                stockService.removeFromBasket(userId, "005930"));

        assertThat(user.getStocks()).doesNotContainKey("005930");
    }

    @Test
    void removeFromBasket_미담긴_종목_STOCK_NOT_FOUND_예외() {
        UUID userId = UUID.randomUUID();
        User user = buildUser(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> stockService.removeFromBasket(userId, "005930"))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode())
                        .isEqualTo(StockErrorCode.STOCK_NOT_FOUND));
    }

    @Test
    void removeFromBasket_미존재_사용자_STOCK_NOT_FOUND_예외() {
        UUID userId = UUID.randomUUID();
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> stockService.removeFromBasket(userId, "005930"))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode())
                        .isEqualTo(StockErrorCode.STOCK_NOT_FOUND));
    }

    // ── getMyBasket ────────────────────────────────────────────────────────────

    @Test
    void getMyBasket_정상_반환() {
        UUID userId = UUID.randomUUID();
        Stock stock1 = buildStock("005930", "삼성전자");
        Stock stock2 = buildStock("035720", "카카오");
        User user = buildUser(userId);
        user.getStocks().put("005930", stock1);
        user.getStocks().put("035720", stock2);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        List<StockResponse> basket = stockService.getMyBasket(userId);

        assertThat(basket).hasSize(2);
        assertThat(basket).extracting(StockResponse::stockCode)
                .containsExactlyInAnyOrder("005930", "035720");
    }

    @Test
    void getMyBasket_빈바구니_빈리스트_반환() {
        UUID userId = UUID.randomUUID();
        User user = buildUser(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        List<StockResponse> basket = stockService.getMyBasket(userId);

        assertThat(basket).isEmpty();
    }

    @Test
    void getMyBasket_미존재_사용자_STOCK_NOT_FOUND_예외() {
        UUID userId = UUID.randomUUID();
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> stockService.getMyBasket(userId))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode())
                        .isEqualTo(StockErrorCode.STOCK_NOT_FOUND));
    }

    // ── getStockDetail ─────────────────────────────────────────────────────────

    @Test
    void getStockDetail_정상_반환() {
        UUID userId = UUID.randomUUID();
        Stock stock = buildStock("005930", "삼성전자");
        when(stockRepository.findById("005930")).thenReturn(Optional.of(stock));

        StockResponse response = stockService.getStockDetail(userId, "005930");

        assertThat(response.stockCode()).isEqualTo("005930");
        assertThat(response.name()).isEqualTo("삼성전자");
    }

    @Test
    void getStockDetail_미존재_종목_STOCK_NOT_FOUND_예외() {
        UUID userId = UUID.randomUUID();
        when(stockRepository.findById("000000")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> stockService.getStockDetail(userId, "000000"))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode())
                        .isEqualTo(StockErrorCode.STOCK_NOT_FOUND));
    }

    // ── searchStocks ───────────────────────────────────────────────────────────

    @Test
    void searchStocks_키워드_매칭_반환() {
        when(stockRepository.findByNameContainingIgnoreCaseOrStockCodeContainingIgnoreCase("삼성", "삼성"))
                .thenReturn(List.of(buildStock("005930", "삼성전자"), buildStock("005935", "삼성전자우")));

        List<StockResponse> results = stockService.searchStocks("삼성");

        assertThat(results).hasSize(2);
    }

    @Test
    void searchStocks_결과없으면_빈리스트() {
        when(stockRepository.findByNameContainingIgnoreCaseOrStockCodeContainingIgnoreCase("없는종목", "없는종목"))
                .thenReturn(List.of());

        List<StockResponse> results = stockService.searchStocks("없는종목");

        assertThat(results).isEmpty();
    }

    // ── helpers ────────────────────────────────────────────────────────────────

    private Stock buildStock(String code, String name) {
        return Stock.builder()
                .stockCode(code)
                .name(name)
                .market(MarketType.KOSPI)
                .build();
    }

    private User buildUser(UUID id) {
        return User.builder()
                .id(id)
                .email("test@example.com")
                .password("encoded")
                .nickname("tester")
                .role(Role.ROLE_USER)
                .build();
    }
}
