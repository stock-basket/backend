package com.hanyahunya.stockbasket.global.security;

import com.hanyahunya.stockbasket.domain.user.entity.Role;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

class UserPrincipalTest {

    @Test
    void 생성자_UUID와_Role_정상_파싱() {
        UUID userId = UUID.randomUUID();

        UserPrincipal principal = new UserPrincipal(userId.toString(), Role.ROLE_USER.name());

        assertThat(principal.getUserId()).isEqualTo(userId);
        assertThat(principal.getRole()).isEqualTo(Role.ROLE_USER);
    }

    @Test
    void 생성자_ADMIN_Role_파싱() {
        UUID userId = UUID.randomUUID();

        UserPrincipal principal = new UserPrincipal(userId.toString(), Role.ROLE_ADMIN.name());

        assertThat(principal.getRole()).isEqualTo(Role.ROLE_ADMIN);
    }

    @Test
    void getAuthorities_권한_하나_반환() {
        UserPrincipal principal = new UserPrincipal(UUID.randomUUID().toString(), Role.ROLE_USER.name());

        Collection<? extends GrantedAuthority> authorities = principal.getAuthorities();

        assertThat(authorities).hasSize(1);
        assertThat(authorities.iterator().next().getAuthority()).isEqualTo(Role.ROLE_USER.name());
    }

    @Test
    void getUsername_userId_문자열_반환() {
        UUID userId = UUID.randomUUID();
        UserPrincipal principal = new UserPrincipal(userId.toString(), Role.ROLE_USER.name());

        assertThat(principal.getUsername()).isEqualTo(userId.toString());
    }

    @Test
    void getPassword_null_반환() {
        UserPrincipal principal = new UserPrincipal(UUID.randomUUID().toString(), Role.ROLE_USER.name());

        assertThat(principal.getPassword()).isNull();
    }

    @Test
    void UserDetails_계정상태_기본값_모두_true() {
        UserPrincipal principal = new UserPrincipal(UUID.randomUUID().toString(), Role.ROLE_USER.name());

        assertThat(principal.isAccountNonExpired()).isTrue();
        assertThat(principal.isAccountNonLocked()).isTrue();
        assertThat(principal.isCredentialsNonExpired()).isTrue();
        assertThat(principal.isEnabled()).isTrue();
    }
}
