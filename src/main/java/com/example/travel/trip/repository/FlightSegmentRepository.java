package com.example.travel.trip.repository;

import com.example.travel.trip.entity.FlightSegment;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FlightSegmentRepository extends JpaRepository<FlightSegment, Long> {

    List<FlightSegment> findAllByTripIdAndDeletedAtIsNull(Long tripId);

    Optional<FlightSegment> findByIdAndTripIdAndDeletedAtIsNull(Long id, Long tripId);
}
