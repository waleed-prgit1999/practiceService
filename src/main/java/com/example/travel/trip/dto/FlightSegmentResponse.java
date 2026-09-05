package com.example.travel.trip.dto;

import com.example.travel.trip.entity.FlightStatus;
import java.time.Instant;

public record FlightSegmentResponse(
        Long id,
        Long tripId,
        String airline,
        String flightNumber,
        String departureAirport,
        String arrivalAirport,
        Instant departureDateTime,
        Instant arrivalDateTime,
        FlightStatus status,
        Instant createdAt,
        Instant updatedAt) {
}
