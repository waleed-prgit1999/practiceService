package com.example.travel.trip.service;

import com.example.travel.audit.entity.AuditAction;
import com.example.travel.audit.service.AuditService;
import com.example.travel.common.event.DomainEventPublisher;
import com.example.travel.common.exception.ResourceNotFoundException;
import com.example.travel.trip.dto.CreateFlightSegmentRequest;
import com.example.travel.trip.dto.FlightSegmentResponse;
import com.example.travel.trip.dto.UpdateFlightSegmentRequest;
import com.example.travel.trip.entity.FlightSegment;
import com.example.travel.trip.entity.Trip;
import com.example.travel.trip.event.FlightAddedEvent;
import com.example.travel.trip.mapper.FlightSegmentMapper;
import com.example.travel.trip.repository.FlightSegmentRepository;
import com.example.travel.trip.validation.FlightSegmentRules;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FlightSegmentService {

    private final FlightSegmentRepository flightSegmentRepository;
    private final FlightSegmentMapper flightSegmentMapper;
    private final TripAccessGuard accessGuard;
    private final TripService tripService;
    private final AuditService auditService;
    private final DomainEventPublisher eventPublisher;

    @Transactional
    public FlightSegmentResponse addFlight(
            Long tripId, CreateFlightSegmentRequest request, Long callerId, boolean isAdmin) {
        Trip trip = accessGuard.loadAccessibleTrip(tripId, callerId, isAdmin);
        accessGuard.assertMutable(trip);
        FlightSegmentRules.validate(
                request.departureAirport(), request.arrivalAirport(), request.departureDateTime(), request.arrivalDateTime());

        FlightSegment flight = flightSegmentMapper.toEntity(request);
        flight.setTrip(trip);
        flight = flightSegmentRepository.save(flight);
        tripService.recalculateTripDates(tripId);

        auditService.record(callerId, AuditAction.FLIGHT_ADDED, "FlightSegment", flight.getId(), null);
        eventPublisher.publish(FlightAddedEvent.now(tripId, flight.getId()));
        return flightSegmentMapper.toResponse(flight);
    }

    @Transactional(readOnly = true)
    public List<FlightSegmentResponse> listFlights(Long tripId, Long callerId, boolean isAdmin) {
        accessGuard.loadAccessibleTrip(tripId, callerId, isAdmin);
        return flightSegmentRepository.findAllByTripIdAndDeletedAtIsNull(tripId).stream()
                .map(flightSegmentMapper::toResponse)
                .toList();
    }

    @Transactional
    public FlightSegmentResponse updateFlight(
            Long tripId, Long flightId, UpdateFlightSegmentRequest request, Long callerId, boolean isAdmin) {
        Trip trip = accessGuard.loadAccessibleTrip(tripId, callerId, isAdmin);
        accessGuard.assertMutable(trip);
        FlightSegmentRules.validate(
                request.departureAirport(), request.arrivalAirport(), request.departureDateTime(), request.arrivalDateTime());

        FlightSegment flight = findFlight(tripId, flightId);
        flightSegmentMapper.updateEntity(request, flight);
        tripService.recalculateTripDates(tripId);

        auditService.record(callerId, AuditAction.FLIGHT_UPDATED, "FlightSegment", flight.getId(), null);
        return flightSegmentMapper.toResponse(flight);
    }

    @Transactional
    public void removeFlight(Long tripId, Long flightId, Long callerId, boolean isAdmin) {
        Trip trip = accessGuard.loadAccessibleTrip(tripId, callerId, isAdmin);
        accessGuard.assertMutable(trip);

        FlightSegment flight = findFlight(tripId, flightId);
        flight.setDeletedAt(Instant.now());
        tripService.recalculateTripDates(tripId);

        auditService.record(callerId, AuditAction.FLIGHT_REMOVED, "FlightSegment", flight.getId(), null);
    }

    private FlightSegment findFlight(Long tripId, Long flightId) {
        return flightSegmentRepository
                .findByIdAndTripIdAndDeletedAtIsNull(flightId, tripId)
                .orElseThrow(() -> ResourceNotFoundException.of("FlightSegment", flightId));
    }
}
