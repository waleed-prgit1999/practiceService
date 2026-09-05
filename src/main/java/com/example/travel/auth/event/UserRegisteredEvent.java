package com.example.travel.auth.event;

import java.time.Instant;

public record UserRegisteredEvent(Long userId, String email, Instant occurredAt) {

    public static UserRegisteredEvent now(Long userId, String email) {
        return new UserRegisteredEvent(userId, email, Instant.now());
    }
}
