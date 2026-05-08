package com.hanyahunya.stockbasket.global.auth;

import com.hanyahunya.stockbasket.domain.user.entity.Role;
import com.hanyahunya.stockbasket.global.exception.BusinessException;
import com.hanyahunya.stockbasket.global.security.UserPrincipal;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

class JwtProviderTest {

    private static final String SECRET =
            "test-secret-key-that-is-long-enough-for-hmac-sha256-at-least-32-chars";
    private static final long VALIDITY_MS = 3_600_000L;

    private JwtProvider jwtProvider;

    @BeforeEach
    void setUp() {
        jwtProvider = new JwtProvider(SECRET, VALIDITY_MS);
    }

    @Test
    void createAccessToken_생성_성공() {
        String token = jwtProvider.createAccessToken(UUID.randomUUID(), Role.ROLE_USER);
        assertThat(token).isNotBlank();
    }

    @Test
    void parseToken_userId_role_정상_파싱() {
        UUID userId = UUID.randomUUID();
        String token = jwtProvider.createAccessToken(userId, Role.ROLE_USER);

        UserPrincipal principal = jwtProvider.parseToken(token);

        assertThat(principal.getUserId()).isEqualTo(userId);
        assertThat(principal.getRole()).isEqualTo(Role.ROLE_USER);
    }

    @Test
    void parseToken_ADMIN_role_파싱() {
        UUID userId = UUID.randomUUID();
        String token = jwtProvider.createAccessToken(userId, Role.ROLE_ADMIN);

        UserPrincipal principal = jwtProvider.parseToken(token);

        assertThat(principal.getRole()).isEqualTo(Role.ROLE_ADMIN);
    }

    @Test
    void parseToken_만료된_토큰_EXPIRED_TOKEN_예외() {
        JwtProvider shortLived = new JwtProvider(SECRET, -1L);
        String token = shortLived.createAccessToken(UUID.randomUUID(), Role.ROLE_USER);

        assertThatThrownBy(() -> jwtProvider.parseToken(token))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode())
                        .isEqualTo(TokenErrorCode.EXPIRED_TOKEN));
    }

    @Test
    void parseToken_잘못된_토큰_INVALID_TOKEN_예외() {
        assertThatThrownBy(() -> jwtProvider.parseToken("invalid.token.value"))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode())
                        .isEqualTo(TokenErrorCode.INVALID_TOKEN));
    }

    @Test
    void parseToken_빈문자열_INVALID_TOKEN_예외() {
        assertThatThrownBy(() -> jwtProvider.parseToken(""))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode())
                        .isEqualTo(TokenErrorCode.INVALID_TOKEN));
    }

    @Test
    void createVerifiedToken_생성_성공() {
        String token = jwtProvider.createVerifiedToken("test@example.com");
        assertThat(token).isNotBlank();
    }

    @Test
    void parseVerifiedToken_이메일_정상_파싱() {
        String email = "verified@example.com";
        String token = jwtProvider.createVerifiedToken(email);

        String parsed = jwtProvider.parseVerifiedToken(token);

        assertThat(parsed).isEqualTo(email);
    }

    @Test
    void parseVerifiedToken_액세스토큰으로_파싱하면_INVALID_TOKEN_예외() {
        String accessToken = jwtProvider.createAccessToken(UUID.randomUUID(), Role.ROLE_USER);

        assertThatThrownBy(() -> jwtProvider.parseVerifiedToken(accessToken))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode())
                        .isEqualTo(TokenErrorCode.INVALID_TOKEN));
    }

    @Test
    void parseVerifiedToken_만료된_인증토큰_EXPIRED_TOKEN_예외() {
        // verified token 의 유효기간은 JwtProvider 내부에 고정되어 있으므로
        // 동일한 시크릿키로 직접 만료된 토큰을 생성하여 테스트
        SecretKey key = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
        Date past = new Date(System.currentTimeMillis() - 10_000L);
        String expiredToken = Jwts.builder()
                .claim("email", "test@example.com")
                .claim("verified", true)
                .issuedAt(past)
                .expiration(new Date(past.getTime() - 1_000L))
                .signWith(key)
                .compact();

        assertThatThrownBy(() -> jwtProvider.parseVerifiedToken(expiredToken))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode())
                        .isEqualTo(TokenErrorCode.EXPIRED_TOKEN));
    }

    @Test
    void validate_유효한_토큰_true() {
        String token = jwtProvider.createAccessToken(UUID.randomUUID(), Role.ROLE_USER);
        assertThat(jwtProvider.validate(token)).isTrue();
    }

    @Test
    void validate_잘못된_토큰_false() {
        assertThat(jwtProvider.validate("bad.token")).isFalse();
    }

    @Test
    void validate_만료된_토큰_false() {
        JwtProvider shortLived = new JwtProvider(SECRET, -1L);
        String token = shortLived.createAccessToken(UUID.randomUUID(), Role.ROLE_USER);
        assertThat(jwtProvider.validate(token)).isFalse();
    }
}
