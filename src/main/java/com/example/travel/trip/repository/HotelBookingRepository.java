package com.example.travel.trip.repository;

import com.example.travel.trip.entity.HotelBooking;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HotelBookingRepository extends JpaRepository<HotelBooking, Long> {

    List<HotelBooking> findAllByTripIdAndDeletedAtIsNull(Long tripId);

    Optional<HotelBooking> findByIdAndTripIdAndDeletedAtIsNull(Long id, Long tripId);
}
