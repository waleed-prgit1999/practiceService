package com.example.travel.trip.event;

import java.time.Instant;

public record FlightAddedEvent(Long tripId, Long flightId, Instant occurredAt) {

    public static FlightAddedEvent now(Long tripId, Long flightId) {
        return new FlightAddedEvent(tripId, flightId, Instant.now());
    }
}
