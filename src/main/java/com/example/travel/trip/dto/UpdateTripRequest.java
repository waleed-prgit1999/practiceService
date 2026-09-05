package com.example.travel.trip.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateTripRequest(
        @NotBlank @Size(max = 150) String name,
        @Size(max = 2000) String description,
        @NotBlank @Size(max = 150) String destination) {
}
