package com.hanyahunya.stockbasket.global.auth;

import com.hanyahunya.stockbasket.domain.user.entity.Role;
import com.hanyahunya.stockbasket.global.exception.BusinessException;
import com.hanyahunya.stockbasket.global.security.UserPrincipal;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

@Slf4j
@Component
public class JwtProvider {

    // 로그인용
    private static final String CLAIM_USER_ID = "userId";
    private static final String CLAIM_ROLE    = "role";
    // 이메일 인증용
    private static final String CLAIM_EMAIL      = "email";
    private static final String CLAIM_VERIFIED   = "verified";
    private static final long VERIFIED_TOKEN_VALID_MS = 10 * 60 * 1000L;

    private final SecretKey secretKey;
    private final long      accessTokenValidMs;

    public JwtProvider(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.access-token-validity-ms}") long accessTokenValidMs
    ) {
        this.secretKey          = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessTokenValidMs = accessTokenValidMs;
    }

    // ── 토큰 발급 ───────────────────────────────────────────────────────────────

    /**
     * access_token 발급
     *
     * @param userId 사용자 UUID
     * @param role   사용자 권한
     * @return 서명된 JWT 문자열
     */
    public String createAccessToken(UUID userId, Role role) {
        Date now    = new Date();
        Date expiry = new Date(now.getTime() + accessTokenValidMs);

        return Jwts.builder()
                .claim(CLAIM_USER_ID, userId.toString())
                .claim(CLAIM_ROLE, role.name())
                .issuedAt(now)
                .expiration(expiry)
                .signWith(secretKey)
                .compact();
    }

    /**
     * 이메일 인증 완료 토큰 발급
     *
     * <p>회원가입 플로우에서 인증 코드 검증 성공 시 발급되며,
     * 이후 회원가입 요청 시 본인 인증 수단으로 사용된다.
     *
     * @param email 인증이 완료된 이메일 주소
     * @return 서명된 JWT 문자열 (유효기간 10분)
     */
    public String createVerifiedToken(String email) {
        Date now = new Date();
        return Jwts.builder()
                .claim(CLAIM_EMAIL, email)
                .claim(CLAIM_VERIFIED, true)
                .issuedAt(now)
                .expiration(new Date(now.getTime() + VERIFIED_TOKEN_VALID_MS))
                .signWith(secretKey)
                .compact();
    }

    // ── 토큰 파싱 ───────────────────────────────────────────────────────────────

    /**
     * 토큰을 파싱하여 UserPrincipal 반환
     *
     * @param token Bearer 이후 원본 토큰 문자열
     * @return UserPrincipal (Spring Security Authentication principal)
     * @throws BusinessException INVALID_TOKEN / EXPIRED_TOKEN
     */
    public UserPrincipal parseToken(String token) {
        Claims claims = getClaims(token);
        String userId = claims.get(CLAIM_USER_ID, String.class);
        String role   = claims.get(CLAIM_ROLE, String.class);
        return new UserPrincipal(userId, role);
    }

    /**
     * 이메일 인증 완료 토큰 파싱
     *
     * @param token 이메일 인증 완료 토큰
     * @return 인증된 이메일 주소
     * @throws BusinessException INVALID_TOKEN — verified 클레임 없거나 위변조된 경우
     * @throws BusinessException EXPIRED_TOKEN — 토큰 만료 (10분 초과)
     */
    public String parseVerifiedToken(String token) {
        Claims claims = getClaims(token); // 만료/위변조 검증 포함
        if (!Boolean.TRUE.equals(claims.get(CLAIM_VERIFIED, Boolean.class))) {
            throw new BusinessException(TokenErrorCode.INVALID_TOKEN);
        }
        return claims.get(CLAIM_EMAIL, String.class);
    }

    /**
     * 토큰 유효성 검사 (파싱 성공 여부)
     */
    public boolean validate(String token) {
        try {
            getClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // ── 내부 유틸 ────────────────────────────────────────────────────────────────

    private Claims getClaims(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (ExpiredJwtException e) {
            throw new com.hanyahunya.stockbasket.global.exception.BusinessException(TokenErrorCode.EXPIRED_TOKEN);
        } catch (JwtException | IllegalArgumentException e) {
            throw new com.hanyahunya.stockbasket.global.exception.BusinessException(TokenErrorCode.INVALID_TOKEN);
        }
    }
}
