package com.example.travel.trip.dto;

import com.example.travel.trip.entity.TripStatus;
import java.time.Instant;

public record TripSummaryResponse(
        Long id,
        Long ownerId,
        String name,
        String destination,
        Instant startDateTime,
        Instant endDateTime,
        TripStatus status,
        Instant createdAt,
        Instant updatedAt) {
}
