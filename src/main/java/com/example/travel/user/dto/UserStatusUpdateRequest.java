package com.example.travel.user.dto;

import com.example.travel.user.entity.UserStatus;
import jakarta.validation.constraints.NotNull;

public record UserStatusUpdateRequest(@NotNull UserStatus status) {
}
