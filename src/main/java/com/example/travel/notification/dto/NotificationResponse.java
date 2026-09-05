package com.example.travel.notification.dto;

import com.example.travel.notification.entity.NotificationType;
import java.time.Instant;

public record NotificationResponse(
        Long id, NotificationType type, String title, String message, boolean read, Instant createdAt) {
}
