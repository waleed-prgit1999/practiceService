package com.example.travel.auth.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.travel.auth.config.JwtProperties;
import com.example.travel.user.entity.Role;
import com.example.travel.user.entity.RoleName;
import com.example.travel.user.entity.User;
import com.example.travel.user.entity.UserStatus;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import java.util.Set;
import org.junit.jupiter.api.Test;

class JwtServiceTest {

    private final JwtProperties properties =
            new JwtProperties("unit-test-signing-secret-must-be-at-least-32-bytes-long", 900_000L, 604_800_000L);
    private final JwtService jwtService = new JwtService(properties);

    @Test
    void generateAndParseAccessToken_roundTripsClaims() {
        User user = user();

        String token = jwtService.generateAccessToken(user);
        Claims claims = jwtService.parseAccessToken(token);

        assertThat(claims.getSubject()).isEqualTo("42");
        assertThat(claims.get("email", String.class)).isEqualTo("test@example.com");
        @SuppressWarnings("unchecked")
        java.util.List<String> roles = (java.util.List<String>) claims.get("roles");
        assertThat(roles).containsExactly("USER");
    }

    @Test
    void parseAccessToken_rejectsRefreshToken() {
        User user = user();
        String refreshToken = jwtService.generateRefreshToken(user);

        assertThatThrownBy(() -> jwtService.parseAccessToken(refreshToken)).isInstanceOf(JwtException.class);
    }

    @Test
    void parseRefreshToken_rejectsAccessToken() {
        User user = user();
        String accessToken = jwtService.generateAccessToken(user);

        assertThatThrownBy(() -> jwtService.parseRefreshToken(accessToken)).isInstanceOf(JwtException.class);
    }

    @Test
    void parseAccessToken_rejectsTamperedToken() {
        User user = user();
        String token = jwtService.generateAccessToken(user);
        String tampered = token.substring(0, token.length() - 1) + (token.endsWith("a") ? "b" : "a");

        assertThatThrownBy(() -> jwtService.parseAccessToken(tampered)).isInstanceOf(JwtException.class);
    }

    private User user() {
        User user = new User();
        user.setId(42L);
        user.setFirstName("Test");
        user.setLastName("User");
        user.setEmail("test@example.com");
        user.setPasswordHash("irrelevant");
        user.setStatus(UserStatus.ACTIVE);
        Role role = new Role();
        role.setId(1L);
        role.setName(RoleName.USER);
        user.setRoles(Set.of(role));
        return user;
    }
}
