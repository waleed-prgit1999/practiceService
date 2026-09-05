package com.example.travel.trip.search;

import com.example.travel.trip.dto.TripSearchCriteria;
import com.example.travel.trip.entity.Trip;
import com.example.travel.trip.repository.TripRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MySqlTripSearchService implements TripSearchService {

    private final TripRepository tripRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<Trip> search(TripSearchCriteria criteria, Pageable pageable) {
        Specification<Trip> specification = Specification.where(TripSpecifications.notDeleted())
                .and(TripSpecifications.hasOwnerId(criteria.ownerId()))
                .and(TripSpecifications.hasStatus(criteria.status()))
                .and(TripSpecifications.hasDestination(criteria.destination()))
                .and(TripSpecifications.startDateFrom(criteria.startDateFrom()))
                .and(TripSpecifications.startDateTo(criteria.startDateTo()))
                .and(TripSpecifications.keyword(criteria.keyword()));
        return tripRepository.findAll(specification, pageable);
    }
}
