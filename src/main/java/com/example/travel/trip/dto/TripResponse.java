package com.example.travel.trip.dto;

import com.example.travel.trip.entity.TripStatus;
import java.time.Instant;
import java.util.List;

public record TripResponse(
        Long id,
        Long ownerId,
        String name,
        String description,
        String destination,
        Instant startDateTime,
        Instant endDateTime,
        TripStatus status,
        List<TravelerResponse> travelers,
        List<FlightSegmentResponse> flights,
        List<HotelBookingResponse> hotels,
        Instant createdAt,
        Instant updatedAt) {
}
