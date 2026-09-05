package com.example.travel.trip.dto;

import com.example.travel.trip.entity.FlightStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Instant;

public record UpdateFlightSegmentRequest(
        @NotBlank @Size(max = 100) String airline,
        @NotBlank @Size(max = 20) String flightNumber,
        @NotBlank @Size(max = 10) String departureAirport,
        @NotBlank @Size(max = 10) String arrivalAirport,
        @NotNull Instant departureDateTime,
        @NotNull Instant arrivalDateTime,
        @NotNull FlightStatus status) {
}
