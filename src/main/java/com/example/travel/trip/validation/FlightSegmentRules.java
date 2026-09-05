package com.example.travel.trip.validation;

import com.example.travel.common.exception.BusinessRuleViolationException;
import java.time.Instant;

public final class FlightSegmentRules {

    private FlightSegmentRules() {
    }

    public static void validate(
            String departureAirport, String arrivalAirport, Instant departureDateTime, Instant arrivalDateTime) {
        if (departureAirport.equalsIgnoreCase(arrivalAirport)) {
            throw new BusinessRuleViolationException("Arrival airport must differ from departure airport");
        }
        if (!arrivalDateTime.isAfter(departureDateTime)) {
            throw new BusinessRuleViolationException("Arrival time must be after departure time");
        }
    }
}
