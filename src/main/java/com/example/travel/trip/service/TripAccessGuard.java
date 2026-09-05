package com.example.travel.trip.service;

import com.example.travel.common.exception.BusinessRuleViolationException;
import com.example.travel.common.exception.ForbiddenOperationException;
import com.example.travel.common.exception.ResourceNotFoundException;
import com.example.travel.trip.entity.Trip;
import com.example.travel.trip.entity.TripStatus;
import com.example.travel.trip.repository.TripRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Ownership and mutability checks shared by TripService and the traveler/flight/hotel services,
 * which all need the identical "does this trip exist, does the caller own it, is it cancelled"
 * checks before touching a trip or one of its sub-resources.
 */
@Component
@RequiredArgsConstructor
class TripAccessGuard {

    private final TripRepository tripRepository;

    Trip loadAccessibleTrip(Long tripId, Long callerId, boolean isAdmin) {
        Trip trip = tripRepository
                .findByIdAndDeletedAtIsNull(tripId)
                .orElseThrow(() -> ResourceNotFoundException.of("Trip", tripId));
        if (!isAdmin && !trip.getOwnerId().equals(callerId)) {
            throw new ForbiddenOperationException("You do not have access to this trip");
        }
        return trip;
    }

    void assertMutable(Trip trip) {
        if (trip.getStatus() == TripStatus.CANCELLED) {
            throw new BusinessRuleViolationException("Cancelled trips cannot be modified");
        }
    }
}
