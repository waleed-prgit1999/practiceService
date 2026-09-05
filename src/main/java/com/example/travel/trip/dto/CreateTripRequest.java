package com.example.travel.trip.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;

public record CreateTripRequest(
        @NotBlank @Size(max = 150) String name,
        @Size(max = 2000) String description,
        @NotBlank @Size(max = 150) String destination,
        @Valid List<CreateFlightSegmentRequest> flights,
        @Valid List<CreateHotelBookingRequest> hotels) {
}
