package com.example.travel.trip.dto;

import com.example.travel.trip.entity.HotelStatus;
import java.time.Instant;

public record HotelBookingResponse(
        Long id,
        Long tripId,
        String hotelName,
        String city,
        String address,
        Instant checkInDateTime,
        Instant checkOutDateTime,
        String confirmationNumber,
        HotelStatus status,
        Instant createdAt,
        Instant updatedAt) {
}
