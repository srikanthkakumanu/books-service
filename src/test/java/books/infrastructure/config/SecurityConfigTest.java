package books.infrastructure.config;

import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;
import java.util.List;
import java.util.Map;
import static org.assertj.core.api.Assertions.assertThat;

class SecurityConfigTest {
    @Test
    void converter_withRealmRolesAndScopes_shouldMapBothAuthorities() {
        var token = Jwt.withTokenValue("test").header("alg", "RS256").subject("user-id")
                .claim("scope", "openid catalog.read")
                .claim("realm_access", Map.of("roles", List.of("USER", "ADMIN"))).build();
        var authentication = new SecurityConfig().jwtAuthenticationConverter().convert(token);
        assertThat(authentication.getAuthorities()).extracting("authority")
                .containsExactlyInAnyOrder("SCOPE_openid", "SCOPE_catalog.read", "ROLE_USER", "ROLE_ADMIN", "FACTOR_BEARER");
    }

    @Test
    void converter_withoutRoleClaims_shouldGrantOnlyBearerFactor() {
        var token = Jwt.withTokenValue("test").header("alg", "RS256").subject("user-id").build();
        assertThat(new SecurityConfig().jwtAuthenticationConverter().convert(token).getAuthorities())
                .extracting("authority").containsExactly("FACTOR_BEARER");
    }
}
