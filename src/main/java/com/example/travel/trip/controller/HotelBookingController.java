package com.example.travel.trip.controller;

import com.example.travel.auth.security.UserPrincipal;
import com.example.travel.trip.dto.CreateHotelBookingRequest;
import com.example.travel.trip.dto.HotelBookingResponse;
import com.example.travel.trip.dto.UpdateHotelBookingRequest;
import com.example.travel.trip.service.HotelBookingService;
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
@RequestMapping("/api/v1/trips/{tripId}/hotels")
@RequiredArgsConstructor
@Tag(name = "Hotels", description = "Manage hotel bookings attached to a trip.")
public class HotelBookingController {

    private final HotelBookingService hotelBookingService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public HotelBookingResponse addHotel(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long tripId,
            @Valid @RequestBody CreateHotelBookingRequest request) {
        return hotelBookingService.addHotel(tripId, request, principal.getId(), principal.hasRole("ADMIN"));
    }

    @GetMapping
    public List<HotelBookingResponse> listHotels(
            @AuthenticationPrincipal UserPrincipal principal, @PathVariable Long tripId) {
        return hotelBookingService.listHotels(tripId, principal.getId(), principal.hasRole("ADMIN"));
    }

    @PutMapping("/{hotelId}")
    public HotelBookingResponse updateHotel(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long tripId,
            @PathVariable Long hotelId,
            @Valid @RequestBody UpdateHotelBookingRequest request) {
        return hotelBookingService.updateHotel(
                tripId, hotelId, request, principal.getId(), principal.hasRole("ADMIN"));
    }

    @DeleteMapping("/{hotelId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeHotel(
            @AuthenticationPrincipal UserPrincipal principal, @PathVariable Long tripId, @PathVariable Long hotelId) {
        hotelBookingService.removeHotel(tripId, hotelId, principal.getId(), principal.hasRole("ADMIN"));
    }
}
