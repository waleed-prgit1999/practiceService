package com.example.travel.trip.controller;

import com.example.travel.auth.security.UserPrincipal;
import com.example.travel.common.dto.PageResponse;
import com.example.travel.trip.dto.CreateTripRequest;
import com.example.travel.trip.dto.TripResponse;
import com.example.travel.trip.dto.TripSearchCriteria;
import com.example.travel.trip.dto.TripSummaryResponse;
import com.example.travel.trip.dto.UpdateTripRequest;
import com.example.travel.trip.entity.TripStatus;
import com.example.travel.trip.service.TripService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneOffset;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/trips")
@RequiredArgsConstructor
@Tag(name = "Trips", description = "Create, search, view, update and cancel trips.")
public class TripController {

    private final TripService tripService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TripResponse createTrip(
            @AuthenticationPrincipal UserPrincipal principal, @Valid @RequestBody CreateTripRequest request) {
        return tripService.createTrip(request, principal.getId());
    }

    @GetMapping
    public PageResponse<TripSummaryResponse> searchTrips(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String destination,
            @RequestParam(required = false) TripStatus status,
            @RequestParam(required = false) LocalDate from,
            @RequestParam(required = false) LocalDate to,
            @RequestParam(required = false) Long ownerId,
            Pageable pageable) {
        TripSearchCriteria criteria = new TripSearchCriteria(
                keyword,
                destination,
                status,
                from != null ? from.atStartOfDay(ZoneOffset.UTC).toInstant() : null,
                to != null ? to.atTime(LocalTime.MAX).atZone(ZoneOffset.UTC).toInstant() : null,
                ownerId);
        return tripService.searchTrips(criteria, pageable, principal.getId(), principal.hasRole("ADMIN"));
    }

    @GetMapping("/{id}")
    public TripResponse getTrip(@AuthenticationPrincipal UserPrincipal principal, @PathVariable Long id) {
        return tripService.getTrip(id, principal.getId(), principal.hasRole("ADMIN"));
    }

    @PutMapping("/{id}")
    public TripResponse updateTrip(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id,
            @Valid @RequestBody UpdateTripRequest request) {
        return tripService.updateTrip(id, request, principal.getId(), principal.hasRole("ADMIN"));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cancelTrip(@AuthenticationPrincipal UserPrincipal principal, @PathVariable Long id) {
        tripService.cancelTrip(id, principal.getId(), principal.hasRole("ADMIN"));
    }
}
