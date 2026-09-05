package com.example.travel.auth.security;

import com.example.travel.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserService userService;

    @Override
    public UserDetails loadUserByUsername(String email) {
        return userService
                .findByEmail(email)
                .map(UserPrincipal::fromUser)
                .orElseThrow(() -> new UsernameNotFoundException("No user with email: " + email));
    }
}
