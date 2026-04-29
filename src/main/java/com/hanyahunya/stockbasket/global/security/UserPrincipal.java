package com.hanyahunya.stockbasket.global.security;

import com.hanyahunya.stockbasket.domain.user.entity.Role;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;
import java.util.UUID;

/**
 * JWT 클레임에서 파싱된 사용자 인증 정보.
 *
 * <p>컨트롤러에서 {@code @AuthenticationPrincipal UserPrincipal principal} 로 주입받아
 * {@link #getUserId()} 및 {@link #getRole()} 로 사용자 정보를 조회한다.
 */
@Getter
public class UserPrincipal implements UserDetails {

    /** JWT claim: "userId" — User 테이블 PK */
    private final UUID userId;

    /** JWT claim: "role" */
    private final Role role;

    /**
     * {@link com.hanyahunya.stockbasket.global.auth.JwtProvider} 에서 호출.
     *
     * @param userIdStr UUID 문자열
     * @param roleStr   Role enum 이름
     */
    public UserPrincipal(String userIdStr, String roleStr) {
        this.userId = UUID.fromString(userIdStr);
        this.role   = Role.valueOf(roleStr);
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority(role.name()));
    }

    // ── UserDetails 기본 구현 (미사용) ──────────────────────────────────────────

    @Override public String  getPassword()             { return null; }
    @Override public String  getUsername()             { return userId.toString(); }
    @Override public boolean isAccountNonExpired()     { return true; }
    @Override public boolean isAccountNonLocked()      { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled()               { return true; }
}
