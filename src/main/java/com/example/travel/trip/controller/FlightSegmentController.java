package com.example.travel.trip.controller;

import com.example.travel.auth.security.UserPrincipal;
import com.example.travel.trip.dto.CreateFlightSegmentRequest;
import com.example.travel.trip.dto.FlightSegmentResponse;
import com.example.travel.trip.dto.UpdateFlightSegmentRequest;
import com.example.travel.trip.service.FlightSegmentService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/trips/{tripId}/flights")
@RequiredArgsConstructor
@Tag(name = "Flights", description = "Manage flight segments attached to a trip.")
public class FlightSegmentController {

    private final FlightSegmentService flightSegmentService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public FlightSegmentResponse addFlight(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long tripId,
            @Valid @RequestBody CreateFlightSegmentRequest request) {
        return flightSegmentService.addFlight(tripId, request, principal.getId(), principal.hasRole("ADMIN"));
    }

    @GetMapping
    public List<FlightSegmentResponse> listFlights(
            @AuthenticationPrincipal UserPrincipal principal, @PathVariable Long tripId) {
        return flightSegmentService.listFlights(tripId, principal.getId(), principal.hasRole("ADMIN"));
    }

    @PutMapping("/{flightId}")
    public FlightSegmentResponse updateFlight(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long tripId,
            @PathVariable Long flightId,
            @Valid @RequestBody UpdateFlightSegmentRequest request) {
        return flightSegmentService.updateFlight(
                tripId, flightId, request, principal.getId(), principal.hasRole("ADMIN"));
    }

    @DeleteMapping("/{flightId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeFlight(
            @AuthenticationPrincipal UserPrincipal principal, @PathVariable Long tripId, @PathVariable Long flightId) {
        flightSegmentService.removeFlight(tripId, flightId, principal.getId(), principal.hasRole("ADMIN"));
    }
}
