package com.example.travel.user.dto;

import com.example.travel.user.entity.RoleName;
import com.example.travel.user.entity.UserStatus;
import java.time.Instant;
import java.util.Set;

public record UserResponse(
        Long id,
        String firstName,
        String lastName,
        String email,
        String phone,
        UserStatus status,
        Set<RoleName> roles,
        Instant createdAt,
        Instant updatedAt) {
}
