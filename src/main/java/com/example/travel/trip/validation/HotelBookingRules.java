package com.example.travel.trip.validation;

import com.example.travel.common.exception.BusinessRuleViolationException;
import java.time.Instant;

public final class HotelBookingRules {

    private HotelBookingRules() {
    }

    public static void validate(Instant checkInDateTime, Instant checkOutDateTime) {
        if (!checkOutDateTime.isAfter(checkInDateTime)) {
            throw new BusinessRuleViolationException("Check-out must be after check-in");
        }
    }
}
