package com.example.travel.auth.security;

import com.example.travel.user.entity.User;
import io.jsonwebtoken.Claims;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

public class UserPrincipal implements UserDetails {

    private final Long id;
    private final String email;
    private final String passwordHash;
    private final boolean enabled;
    private final Collection<? extends GrantedAuthority> authorities;

    private UserPrincipal(
            Long id, String email, String passwordHash, boolean enabled, Collection<? extends GrantedAuthority> authorities) {
        this.id = id;
        this.email = email;
        this.passwordHash = passwordHash;
        this.enabled = enabled;
        this.authorities = authorities;
    }

    public static UserPrincipal fromUser(User user) {
        Collection<? extends GrantedAuthority> authorities = user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.getName().name()))
                .collect(Collectors.toSet());
        return new UserPrincipal(user.getId(), user.getEmail(), user.getPasswordHash(), user.isActive(), authorities);
    }

    /**
     * Built from a validated access token's claims, without a database round trip. Roles reflect
     * what was true at token issuance; a status change (e.g. disabling a user) takes effect on the
     * next token refresh, not mid-flight for an already-issued access token.
     */
    @SuppressWarnings("unchecked")
    public static UserPrincipal fromClaims(Claims claims) {
        Long id = Long.valueOf(claims.getSubject());
        String email = claims.get("email", String.class);
        List<String> roleNames = claims.get("roles", List.class);
        Collection<? extends GrantedAuthority> authorities = roleNames.stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                .collect(Collectors.toSet());
        return new UserPrincipal(id, email, null, true, authorities);
    }

    public Long getId() {
        return id;
    }

    public boolean hasRole(String role) {
        return authorities.stream().anyMatch(a -> a.getAuthority().equals("ROLE_" + role));
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return passwordHash;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }
}
