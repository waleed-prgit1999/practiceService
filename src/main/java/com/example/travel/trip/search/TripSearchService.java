package com.example.travel.trip.search;

import com.example.travel.trip.dto.TripSearchCriteria;
import com.example.travel.trip.entity.Trip;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Search port for trips. {@link MySqlTripSearchService} is the Phase 1 implementation; a future
 * {@code ElasticsearchTripSearchService} can implement this same contract (see
 * docs/architecture/decisions.md, "Why a search abstraction now?"). Callers must depend only on
 * this interface, never on the MySQL implementation.
 */
public interface TripSearchService {

    Page<Trip> search(TripSearchCriteria criteria, Pageable pageable);
}
