package com.example.travel.trip.event;

import java.time.Instant;

public record TripCreatedEvent(Long tripId, Long ownerId, Instant occurredAt) {

    public static TripCreatedEvent now(Long tripId, Long ownerId) {
        return new TripCreatedEvent(tripId, ownerId, Instant.now());
    }
}
