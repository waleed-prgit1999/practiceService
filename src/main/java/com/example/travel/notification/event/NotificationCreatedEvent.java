package com.example.travel.notification.event;

import java.time.Instant;

public record NotificationCreatedEvent(Long notificationId, Long userId, Instant occurredAt) {

    public static NotificationCreatedEvent now(Long notificationId, Long userId) {
        return new NotificationCreatedEvent(notificationId, userId, Instant.now());
    }
}
