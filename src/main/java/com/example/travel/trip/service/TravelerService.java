package com.example.travel.trip.service;

import com.example.travel.audit.entity.AuditAction;
import com.example.travel.audit.service.AuditService;
import com.example.travel.common.exception.ResourceNotFoundException;
import com.example.travel.trip.dto.CreateTravelerRequest;
import com.example.travel.trip.dto.TravelerResponse;
import com.example.travel.trip.dto.UpdateTravelerRequest;
import com.example.travel.trip.entity.Traveler;
import com.example.travel.trip.entity.Trip;
import com.example.travel.trip.mapper.TravelerMapper;
import com.example.travel.trip.repository.TravelerRepository;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TravelerService {

    private final TravelerRepository travelerRepository;
    private final TravelerMapper travelerMapper;
    private final TripAccessGuard accessGuard;
    private final AuditService auditService;

    @Transactional
    public TravelerResponse addTraveler(Long tripId, CreateTravelerRequest request, Long callerId, boolean isAdmin) {
        Trip trip = accessGuard.loadAccessibleTrip(tripId, callerId, isAdmin);
        accessGuard.assertMutable(trip);

        Traveler traveler = travelerMapper.toEntity(request);
        traveler.setTrip(trip);
        traveler = travelerRepository.save(traveler);

        auditService.record(callerId, AuditAction.TRAVELER_ADDED, "Traveler", traveler.getId(), null);
        return travelerMapper.toResponse(traveler);
    }

    @Transactional(readOnly = true)
    public List<TravelerResponse> listTravelers(Long tripId, Long callerId, boolean isAdmin) {
        accessGuard.loadAccessibleTrip(tripId, callerId, isAdmin);
        return travelerRepository.findAllByTripIdAndDeletedAtIsNull(tripId).stream()
                .map(travelerMapper::toResponse)
                .toList();
    }

    @Transactional
    public TravelerResponse updateTraveler(
            Long tripId, Long travelerId, UpdateTravelerRequest request, Long callerId, boolean isAdmin) {
        Trip trip = accessGuard.loadAccessibleTrip(tripId, callerId, isAdmin);
        accessGuard.assertMutable(trip);

        Traveler traveler = findTraveler(tripId, travelerId);
        travelerMapper.updateEntity(request, traveler);

        auditService.record(callerId, AuditAction.TRAVELER_UPDATED, "Traveler", traveler.getId(), null);
        return travelerMapper.toResponse(traveler);
    }

    @Transactional
    public void removeTraveler(Long tripId, Long travelerId, Long callerId, boolean isAdmin) {
        Trip trip = accessGuard.loadAccessibleTrip(tripId, callerId, isAdmin);
        accessGuard.assertMutable(trip);

        Traveler traveler = findTraveler(tripId, travelerId);
        traveler.setDeletedAt(Instant.now());

        auditService.record(callerId, AuditAction.TRAVELER_REMOVED, "Traveler", traveler.getId(), null);
    }

    private Traveler findTraveler(Long tripId, Long travelerId) {
        return travelerRepository
                .findByIdAndTripIdAndDeletedAtIsNull(travelerId, tripId)
                .orElseThrow(() -> ResourceNotFoundException.of("Traveler", travelerId));
    }
}
