package com.example.travel.auth.security;

import com.example.travel.auth.config.JwtProperties;
import com.example.travel.user.entity.Role;
import com.example.travel.user.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import javax.crypto.SecretKey;
import org.springframework.stereotype.Service;

/**
 * Issues and validates JWTs. Access and refresh tokens are both JWTs signed with the same secret,
 * distinguished by a {@code type} claim; refresh tokens are not persisted (see
 * docs/architecture/decisions.md, "Refresh tokens are stateless").
 */
@Service
public class JwtService {

    private static final String CLAIM_TYPE = "type";
    private static final String CLAIM_EMAIL = "email";
    private static final String CLAIM_ROLES = "roles";

    private final SecretKey key;
    private final JwtProperties properties;

    public JwtService(JwtProperties properties) {
        this.properties = properties;
        this.key = Keys.hmacShaKeyFor(properties.secret().getBytes(StandardCharsets.UTF_8));
    }

    public String generateAccessToken(User user) {
        return generateToken(user, TokenType.ACCESS, properties.accessTokenExpirationMs());
    }

    public String generateRefreshToken(User user) {
        return generateToken(user, TokenType.REFRESH, properties.refreshTokenExpirationMs());
    }

    public long getAccessTokenExpirationMs() {
        return properties.accessTokenExpirationMs();
    }

    public Claims parseAccessToken(String token) {
        Claims claims = parse(token);
        requireType(claims, TokenType.ACCESS);
        return claims;
    }

    public Claims parseRefreshToken(String token) {
        Claims claims = parse(token);
        requireType(claims, TokenType.REFRESH);
        return claims;
    }

    private String generateToken(User user, TokenType type, long expirationMs) {
        Instant now = Instant.now();
        List<String> roleNames = user.getRoles().stream().map(Role::getName).map(Enum::name).toList();
        return Jwts.builder()
                .subject(String.valueOf(user.getId()))
                .claim(CLAIM_EMAIL, user.getEmail())
                .claim(CLAIM_ROLES, roleNames)
                .claim(CLAIM_TYPE, type.name())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusMillis(expirationMs)))
                .signWith(key)
                .compact();
    }

    private Claims parse(String token) {
        return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
    }

    private void requireType(Claims claims, TokenType expected) {
        String actual = claims.get(CLAIM_TYPE, String.class);
        if (!expected.name().equals(actual)) {
            throw new JwtException("Expected a " + expected.name() + " token but got " + actual);
        }
    }
}
