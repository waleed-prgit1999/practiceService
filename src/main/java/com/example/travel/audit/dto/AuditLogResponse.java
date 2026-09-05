package com.example.travel.audit.dto;

import java.time.Instant;
import java.util.Map;

public record AuditLogResponse(
        Long id,
        Long userId,
        String action,
        String entityType,
        Long entityId,
        Map<String, Object> metadata,
        Instant createdAt) {
}
