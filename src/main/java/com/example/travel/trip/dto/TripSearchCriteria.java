package com.example.travel.trip.dto;

import com.example.travel.trip.entity.TripStatus;
import java.time.Instant;

public record TripSearchCriteria(
        String keyword,
        String destination,
        TripStatus status,
        Instant startDateFrom,
        Instant startDateTo,
        Long ownerId) {
}
