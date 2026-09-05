package com.example.travel.trip.repository;

import com.example.travel.trip.entity.Trip;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface TripRepository extends JpaRepository<Trip, Long>, JpaSpecificationExecutor<Trip> {

    Optional<Trip> findByIdAndDeletedAtIsNull(Long id);
}
