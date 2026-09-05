package com.example.travel.trip.event;

import java.time.Instant;

public record TripCancelledEvent(Long tripId, Long ownerId, Instant occurredAt) {

    public static TripCancelledEvent now(Long tripId, Long ownerId) {
        return new TripCancelledEvent(tripId, ownerId, Instant.now());
    }
}
