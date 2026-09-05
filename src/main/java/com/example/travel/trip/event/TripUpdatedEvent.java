package com.example.travel.trip.event;

import java.time.Instant;

public record TripUpdatedEvent(Long tripId, Long ownerId, Instant occurredAt) {

    public static TripUpdatedEvent now(Long tripId, Long ownerId) {
        return new TripUpdatedEvent(tripId, ownerId, Instant.now());
    }
}
