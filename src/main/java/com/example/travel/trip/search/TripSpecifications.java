package com.example.travel.trip.search;

import com.example.travel.trip.entity.Trip;
import com.example.travel.trip.entity.TripStatus;
import java.time.Instant;
import java.util.Locale;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

final class TripSpecifications {

    private TripSpecifications() {
    }

    static Specification<Trip> notDeleted() {
        return (root, query, cb) -> cb.isNull(root.get("deletedAt"));
    }

    static Specification<Trip> hasOwnerId(Long ownerId) {
        return ownerId == null ? null : (root, query, cb) -> cb.equal(root.get("ownerId"), ownerId);
    }

    static Specification<Trip> hasStatus(TripStatus status) {
        return status == null ? null : (root, query, cb) -> cb.equal(root.get("status"), status);
    }

    static Specification<Trip> hasDestination(String destination) {
        if (!StringUtils.hasText(destination)) {
            return null;
        }
        String normalized = destination.toLowerCase(Locale.ROOT);
        return (root, query, cb) -> cb.equal(cb.lower(root.get("destination")), normalized);
    }

    static Specification<Trip> startDateFrom(Instant from) {
        return from == null ? null : (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("startDateTime"), from);
    }

    static Specification<Trip> startDateTo(Instant to) {
        return to == null ? null : (root, query, cb) -> cb.lessThanOrEqualTo(root.get("startDateTime"), to);
    }

    static Specification<Trip> keyword(String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return null;
        }
        String like = "%" + keyword.toLowerCase(Locale.ROOT) + "%";
        return (root, query, cb) -> cb.or(
                cb.like(cb.lower(root.get("name")), like),
                cb.like(cb.lower(root.get("description")), like),
                cb.like(cb.lower(root.get("destination")), like));
    }
}
