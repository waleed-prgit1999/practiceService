package com.example.travel.trip.repository;

import com.example.travel.trip.entity.Traveler;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TravelerRepository extends JpaRepository<Traveler, Long> {

    List<Traveler> findAllByTripIdAndDeletedAtIsNull(Long tripId);

    Optional<Traveler> findByIdAndTripIdAndDeletedAtIsNull(Long id, Long tripId);
}
