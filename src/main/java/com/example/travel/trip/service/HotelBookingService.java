package com.example.travel.trip.service;

import com.example.travel.audit.entity.AuditAction;
import com.example.travel.audit.service.AuditService;
import com.example.travel.common.event.DomainEventPublisher;
import com.example.travel.common.exception.ResourceNotFoundException;
import com.example.travel.trip.dto.CreateHotelBookingRequest;
import com.example.travel.trip.dto.HotelBookingResponse;
import com.example.travel.trip.dto.UpdateHotelBookingRequest;
import com.example.travel.trip.entity.HotelBooking;
import com.example.travel.trip.entity.Trip;
import com.example.travel.trip.event.HotelAddedEvent;
import com.example.travel.trip.mapper.HotelBookingMapper;
import com.example.travel.trip.repository.HotelBookingRepository;
import com.example.travel.trip.validation.HotelBookingRules;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class HotelBookingService {

    private final HotelBookingRepository hotelBookingRepository;
    private final HotelBookingMapper hotelBookingMapper;
    private final TripAccessGuard accessGuard;
    private final TripService tripService;
    private final AuditService auditService;
    private final DomainEventPublisher eventPublisher;

    @Transactional
    public HotelBookingResponse addHotel(
            Long tripId, CreateHotelBookingRequest request, Long callerId, boolean isAdmin) {
        Trip trip = accessGuard.loadAccessibleTrip(tripId, callerId, isAdmin);
        accessGuard.assertMutable(trip);
        HotelBookingRules.validate(request.checkInDateTime(), request.checkOutDateTime());

        HotelBooking hotel = hotelBookingMapper.toEntity(request);
        hotel.setTrip(trip);
        hotel = hotelBookingRepository.save(hotel);
        tripService.recalculateTripDates(tripId);

        auditService.record(callerId, AuditAction.HOTEL_ADDED, "HotelBooking", hotel.getId(), null);
        eventPublisher.publish(HotelAddedEvent.now(tripId, hotel.getId()));
        return hotelBookingMapper.toResponse(hotel);
    }

    @Transactional(readOnly = true)
    public List<HotelBookingResponse> listHotels(Long tripId, Long callerId, boolean isAdmin) {
        accessGuard.loadAccessibleTrip(tripId, callerId, isAdmin);
        return hotelBookingRepository.findAllByTripIdAndDeletedAtIsNull(tripId).stream()
                .map(hotelBookingMapper::toResponse)
                .toList();
    }

    @Transactional
    public HotelBookingResponse updateHotel(
            Long tripId, Long hotelId, UpdateHotelBookingRequest request, Long callerId, boolean isAdmin) {
        Trip trip = accessGuard.loadAccessibleTrip(tripId, callerId, isAdmin);
        accessGuard.assertMutable(trip);
        HotelBookingRules.validate(request.checkInDateTime(), request.checkOutDateTime());

        HotelBooking hotel = findHotel(tripId, hotelId);
        hotelBookingMapper.updateEntity(request, hotel);
        tripService.recalculateTripDates(tripId);

        auditService.record(callerId, AuditAction.HOTEL_UPDATED, "HotelBooking", hotel.getId(), null);
        return hotelBookingMapper.toResponse(hotel);
    }

    @Transactional
    public void removeHotel(Long tripId, Long hotelId, Long callerId, boolean isAdmin) {
        Trip trip = accessGuard.loadAccessibleTrip(tripId, callerId, isAdmin);
        accessGuard.assertMutable(trip);

        HotelBooking hotel = findHotel(tripId, hotelId);
        hotel.setDeletedAt(Instant.now());
        tripService.recalculateTripDates(tripId);

        auditService.record(callerId, AuditAction.HOTEL_REMOVED, "HotelBooking", hotel.getId(), null);
    }

    private HotelBooking findHotel(Long tripId, Long hotelId) {
        return hotelBookingRepository
                .findByIdAndTripIdAndDeletedAtIsNull(hotelId, tripId)
                .orElseThrow(() -> ResourceNotFoundException.of("HotelBooking", hotelId));
    }
}
