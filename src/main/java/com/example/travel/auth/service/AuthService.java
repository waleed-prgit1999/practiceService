package com.example.travel.auth.service;

import com.example.travel.audit.entity.AuditAction;
import com.example.travel.audit.service.AuditService;
import com.example.travel.auth.dto.AuthResponse;
import com.example.travel.auth.dto.LoginRequest;
import com.example.travel.auth.dto.RefreshRequest;
import com.example.travel.auth.dto.RegisterRequest;
import com.example.travel.auth.event.UserRegisteredEvent;
import com.example.travel.auth.security.JwtService;
import com.example.travel.auth.security.UserPrincipal;
import com.example.travel.common.event.DomainEventPublisher;
import com.example.travel.common.exception.ResourceNotFoundException;
import com.example.travel.user.dto.UserResponse;
import com.example.travel.user.entity.User;
import com.example.travel.user.mapper.UserMapper;
import com.example.travel.user.service.UserService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import java.util.Locale;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserService userService;
    private final UserMapper userMapper;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuditService auditService;
    private final DomainEventPublisher eventPublisher;

    @Transactional
    public UserResponse register(RegisterRequest request) {
        String passwordHash = passwordEncoder.encode(request.password());
        User user = userService.registerNewUser(
                request.firstName(), request.lastName(), request.email(), passwordHash, request.phone());
        auditService.record(user.getId(), AuditAction.USER_REGISTERED, "User", user.getId(), null);
        eventPublisher.publish(UserRegisteredEvent.now(user.getId(), user.getEmail()));
        return userMapper.toResponse(user);
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        String email = normalizeEmail(request.email());
        try {
            var authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, request.password()));
            UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
            User user = userService.getEntityById(principal.getId());
            auditService.record(user.getId(), AuditAction.LOGIN_SUCCESS, "User", user.getId(), null);
            return issueTokens(user);
        } catch (AuthenticationException ex) {
            Long userId = userService.findByEmail(email).map(User::getId).orElse(null);
            auditService.record(userId, AuditAction.LOGIN_FAILED, "User", userId, Map.of("email", email));
            throw ex;
        }
    }

    @Transactional(readOnly = true)
    public AuthResponse refresh(RefreshRequest request) {
        Claims claims;
        try {
            claims = jwtService.parseRefreshToken(request.refreshToken());
        } catch (JwtException | IllegalArgumentException ex) {
            throw new BadCredentialsException("Invalid or expired refresh token");
        }

        User user;
        try {
            user = userService.getEntityById(Long.valueOf(claims.getSubject()));
        } catch (ResourceNotFoundException ex) {
            throw new BadCredentialsException("Invalid or expired refresh token");
        }
        if (!user.isActive()) {
            throw new BadCredentialsException("User account is disabled");
        }
        return issueTokens(user);
    }

    private AuthResponse issueTokens(User user) {
        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);
        long expiresInSeconds = jwtService.getAccessTokenExpirationMs() / 1000;
        return new AuthResponse(accessToken, refreshToken, "Bearer", expiresInSeconds);
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
