package com.example.travel.trip.controller;

import com.example.travel.auth.security.UserPrincipal;
import com.example.travel.trip.dto.CreateTravelerRequest;
import com.example.travel.trip.dto.TravelerResponse;
import com.example.travel.trip.dto.UpdateTravelerRequest;
import com.example.travel.trip.service.TravelerService;
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
@RequestMapping("/api/v1/trips/{tripId}/travelers")
@RequiredArgsConstructor
@Tag(name = "Travelers", description = "Manage travelers attached to a trip.")
public class TravelerController {

    private final TravelerService travelerService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TravelerResponse addTraveler(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long tripId,
            @Valid @RequestBody CreateTravelerRequest request) {
        return travelerService.addTraveler(tripId, request, principal.getId(), principal.hasRole("ADMIN"));
    }

    @GetMapping
    public List<TravelerResponse> listTravelers(
            @AuthenticationPrincipal UserPrincipal principal, @PathVariable Long tripId) {
        return travelerService.listTravelers(tripId, principal.getId(), principal.hasRole("ADMIN"));
    }

    @PutMapping("/{travelerId}")
    public TravelerResponse updateTraveler(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long tripId,
            @PathVariable Long travelerId,
            @Valid @RequestBody UpdateTravelerRequest request) {
        return travelerService.updateTraveler(
                tripId, travelerId, request, principal.getId(), principal.hasRole("ADMIN"));
    }

    @DeleteMapping("/{travelerId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeTraveler(
            @AuthenticationPrincipal UserPrincipal principal, @PathVariable Long tripId, @PathVariable Long travelerId) {
        travelerService.removeTraveler(tripId, travelerId, principal.getId(), principal.hasRole("ADMIN"));
    }
}
