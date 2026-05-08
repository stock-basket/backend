package com.hanyahunya.stockbasket.domain.user.entity;

import com.hanyahunya.stockbasket.domain.stock.entity.MarketType;
import com.hanyahunya.stockbasket.domain.stock.entity.Stock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

class UserTest {

    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(UUID.randomUUID())
                .email("test@example.com")
                .password("encoded-password")
                .nickname("tester")
                .role(Role.ROLE_USER)
                .build();
    }

    @Test
    void updateNickname_닉네임_변경() {
        user.updateNickname("newNick");

        assertThat(user.getNickname()).isEqualTo("newNick");
    }

    @Test
    void updatePassword_비밀번호_변경() {
        user.updatePassword("new-encoded-password");

        assertThat(user.getPassword()).isEqualTo("new-encoded-password");
    }

    @Test
    void getStocks_기본값_빈맵() {
        assertThat(user.getStocks()).isEmpty();
    }

    @Test
    void getStocks_종목_추가() {
        Stock stock = Stock.builder()
                .stockCode("005930").name("삼성전자").market(MarketType.KOSPI).build();

        user.getStocks().put("005930", stock);

        assertThat(user.getStocks()).containsKey("005930");
    }

    @Test
    void builder_모든_필드_설정() {
        UUID id = UUID.randomUUID();
        User built = User.builder()
                .id(id)
                .email("user@test.com")
                .password("pass")
                .nickname("nick")
                .role(Role.ROLE_ADMIN)
                .build();

        assertThat(built.getId()).isEqualTo(id);
        assertThat(built.getEmail()).isEqualTo("user@test.com");
        assertThat(built.getRole()).isEqualTo(Role.ROLE_ADMIN);
    }
}
