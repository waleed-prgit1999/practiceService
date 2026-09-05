package com.example.travel.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Admin-side user creation. Intentionally has no password field: the account is created with the
 * configured default password (app.security.default-new-user-password), and the created account
 * always gets the USER role — this endpoint cannot mint administrators.
 */
public record AdminCreateUserRequest(
        @NotBlank @Size(max = 100) String firstName,
        @NotBlank @Size(max = 100) String lastName,
        @NotBlank @Email @Size(max = 255) String email,
        @Size(max = 30) String phone) {
}
