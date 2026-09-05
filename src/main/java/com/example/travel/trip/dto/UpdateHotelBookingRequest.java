package com.example.travel.trip.dto;

import com.example.travel.trip.entity.HotelStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Instant;

public record UpdateHotelBookingRequest(
        @NotBlank @Size(max = 150) String hotelName,
        @NotBlank @Size(max = 100) String city,
        @Size(max = 255) String address,
        @NotNull Instant checkInDateTime,
        @NotNull Instant checkOutDateTime,
        @Size(max = 50) String confirmationNumber,
        @NotNull HotelStatus status) {
}
