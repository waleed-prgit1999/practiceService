package com.example.travel.trip.event;

import java.time.Instant;

public record HotelAddedEvent(Long tripId, Long hotelId, Instant occurredAt) {

    public static HotelAddedEvent now(Long tripId, Long hotelId) {
        return new HotelAddedEvent(tripId, hotelId, Instant.now());
    }
}
