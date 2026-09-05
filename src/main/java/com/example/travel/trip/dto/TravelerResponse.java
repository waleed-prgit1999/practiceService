package com.example.travel.trip.dto;

import java.time.Instant;
import java.time.LocalDate;

public record TravelerResponse(
        Long id,
        Long tripId,
        String firstName,
        String lastName,
        String email,
        LocalDate dateOfBirth,
        String passportNumber,
        String nationality,
        Instant createdAt,
        Instant updatedAt) {
}
