package com.example.travel.trip.service;

import com.example.travel.audit.entity.AuditAction;
import com.example.travel.audit.service.AuditService;
import com.example.travel.common.dto.PageResponse;
import com.example.travel.common.event.DomainEventPublisher;
import com.example.travel.common.exception.BusinessRuleViolationException;
import com.example.travel.trip.dto.CreateFlightSegmentRequest;
import com.example.travel.trip.dto.CreateHotelBookingRequest;
import com.example.travel.trip.dto.CreateTripRequest;
import com.example.travel.trip.dto.TripResponse;
import com.example.travel.trip.dto.TripSearchCriteria;
import com.example.travel.trip.dto.TripSummaryResponse;
import com.example.travel.trip.dto.UpdateTripRequest;
import com.example.travel.trip.entity.FlightSegment;
import com.example.travel.trip.entity.HotelBooking;
import com.example.travel.trip.entity.Traveler;
import com.example.travel.trip.entity.Trip;
import com.example.travel.trip.entity.TripStatus;
import com.example.travel.trip.event.TripCancelledEvent;
import com.example.travel.trip.event.TripCreatedEvent;
import com.example.travel.trip.event.TripUpdatedEvent;
import com.example.travel.trip.mapper.FlightSegmentMapper;
import com.example.travel.trip.mapper.HotelBookingMapper;
import com.example.travel.trip.mapper.TravelerMapper;
import com.example.travel.trip.mapper.TripMapper;
import com.example.travel.trip.repository.FlightSegmentRepository;
import com.example.travel.trip.repository.HotelBookingRepository;
import com.example.travel.trip.repository.TravelerRepository;
import com.example.travel.trip.repository.TripRepository;
import com.example.travel.trip.search.TripSearchService;
import com.example.travel.trip.validation.FlightSegmentRules;
import com.example.travel.trip.validation.HotelBookingRules;
import com.example.travel.notification.entity.NotificationType;
import com.example.travel.notification.service.NotificationService;
import com.example.travel.user.service.UserService;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TripService {

    private final TripRepository tripRepository;
    private final TravelerRepository travelerRepository;
    private final FlightSegmentRepository flightSegmentRepository;
    private final HotelBookingRepository hotelBookingRepository;
    private final TripMapper tripMapper;
    private final TravelerMapper travelerMapper;
    private final FlightSegmentMapper flightSegmentMapper;
    private final HotelBookingMapper hotelBookingMapper;
    private final TripAccessGuard accessGuard;
    private final TripSearchService tripSearchService;
    private final UserService userService;
    private final NotificationService notificationService;
    private final AuditService auditService;
    private final DomainEventPublisher eventPublisher;

    @Transactional
    public TripResponse createTrip(CreateTripRequest request, Long ownerId) {
        if (!userService.getEntityById(ownerId).isActive()) {
            throw new BusinessRuleViolationException("Disabled users cannot create trips");
        }

        Trip trip = tripMapper.toEntity(request);
        trip.setOwnerId(ownerId);
        trip.setStatus(TripStatus.PLANNED);
        trip = tripRepository.save(trip);

        List<FlightSegment> flights = createFlights(trip, request.flights());
        List<HotelBooking> hotels = createHotels(trip, request.hotels());
        applyComputedDates(trip, flights, hotels);

        auditService.record(ownerId, AuditAction.TRIP_CREATED, "Trip", trip.getId(), null);
        eventPublisher.publish(TripCreatedEvent.now(trip.getId(), ownerId));
        notificationService.create(
                ownerId, NotificationType.TRIP, "Trip created", "Your trip '" + trip.getName() + "' has been created.");

        return assembleResponse(trip, List.of(), flights, hotels);
    }

    @Transactional(readOnly = true)
    public TripResponse getTrip(Long tripId, Long callerId, boolean isAdmin) {
        Trip trip = accessGuard.loadAccessibleTrip(tripId, callerId, isAdmin);
        return assembleResponse(
                trip,
                travelerRepository.findAllByTripIdAndDeletedAtIsNull(tripId),
                flightSegmentRepository.findAllByTripIdAndDeletedAtIsNull(tripId),
                hotelBookingRepository.findAllByTripIdAndDeletedAtIsNull(tripId));
    }

    @Transactional
    public TripResponse updateTrip(Long tripId, UpdateTripRequest request, Long callerId, boolean isAdmin) {
        Trip trip = accessGuard.loadAccessibleTrip(tripId, callerId, isAdmin);
        accessGuard.assertMutable(trip);

        tripMapper.updateEntity(request, trip);

        auditService.record(callerId, AuditAction.TRIP_UPDATED, "Trip", trip.getId(), null);
        eventPublisher.publish(TripUpdatedEvent.now(trip.getId(), trip.getOwnerId()));

        return assembleResponse(
                trip,
                travelerRepository.findAllByTripIdAndDeletedAtIsNull(tripId),
                flightSegmentRepository.findAllByTripIdAndDeletedAtIsNull(tripId),
                hotelBookingRepository.findAllByTripIdAndDeletedAtIsNull(tripId));
    }

    @Transactional
    public void cancelTrip(Long tripId, Long callerId, boolean isAdmin) {
        Trip trip = accessGuard.loadAccessibleTrip(tripId, callerId, isAdmin);
        if (trip.getStatus() == TripStatus.CANCELLED) {
            return;
        }

        Instant now = Instant.now();
        trip.setStatus(TripStatus.CANCELLED);
        trip.setDeletedAt(now);
        travelerRepository.findAllByTripIdAndDeletedAtIsNull(tripId).forEach(t -> t.setDeletedAt(now));
        flightSegmentRepository.findAllByTripIdAndDeletedAtIsNull(tripId).forEach(f -> f.setDeletedAt(now));
        hotelBookingRepository.findAllByTripIdAndDeletedAtIsNull(tripId).forEach(h -> h.setDeletedAt(now));

        auditService.record(callerId, AuditAction.TRIP_CANCELLED, "Trip", trip.getId(), null);
        eventPublisher.publish(TripCancelledEvent.now(trip.getId(), trip.getOwnerId()));
    }

    @Transactional(readOnly = true)
    public PageResponse<TripSummaryResponse> searchTrips(
            TripSearchCriteria rawCriteria, Pageable pageable, Long callerId, boolean isAdmin) {
        Long ownerFilter = isAdmin ? rawCriteria.ownerId() : callerId;
        TripSearchCriteria criteria = new TripSearchCriteria(
                rawCriteria.keyword(),
                rawCriteria.destination(),
                rawCriteria.status(),
                rawCriteria.startDateFrom(),
                rawCriteria.startDateTo(),
                ownerFilter);
        Page<Trip> page = tripSearchService.search(criteria, pageable);
        return PageResponse.of(page, tripMapper::toSummary);
    }

    /**
     * Recomputes a trip's start/end window from its non-deleted flight segments and hotel
     * bookings (min departure/check-in, max arrival/check-out). Called by the flight and hotel
     * services after any create/update/delete so the trip's dates stay in sync (see plan.md 5.3).
     */
    @Transactional
    void recalculateTripDates(Long tripId) {
        Trip trip = tripRepository.findByIdAndDeletedAtIsNull(tripId).orElse(null);
        if (trip == null) {
            return;
        }
        applyComputedDates(
                trip,
                flightSegmentRepository.findAllByTripIdAndDeletedAtIsNull(tripId),
                hotelBookingRepository.findAllByTripIdAndDeletedAtIsNull(tripId));
    }

    private void applyComputedDates(Trip trip, List<FlightSegment> flights, List<HotelBooking> hotels) {
        Instant start = Stream.concat(
                        flights.stream().map(FlightSegment::getDepartureDateTime),
                        hotels.stream().map(HotelBooking::getCheckInDateTime))
                .min(Comparator.naturalOrder())
                .orElse(null);
        Instant end = Stream.concat(
                        flights.stream().map(FlightSegment::getArrivalDateTime),
                        hotels.stream().map(HotelBooking::getCheckOutDateTime))
                .max(Comparator.naturalOrder())
                .orElse(null);
        trip.setStartDateTime(start);
        trip.setEndDateTime(end);
    }

    private List<FlightSegment> createFlights(Trip trip, List<CreateFlightSegmentRequest> requests) {
        if (requests == null || requests.isEmpty()) {
            return List.of();
        }
        List<FlightSegment> flights = requests.stream()
                .map(r -> {
                    FlightSegmentRules.validate(
                            r.departureAirport(), r.arrivalAirport(), r.departureDateTime(), r.arrivalDateTime());
                    FlightSegment flight = flightSegmentMapper.toEntity(r);
                    flight.setTrip(trip);
                    return flight;
                })
                .toList();
        return flightSegmentRepository.saveAll(flights);
    }

    private List<HotelBooking> createHotels(Trip trip, List<CreateHotelBookingRequest> requests) {
        if (requests == null || requests.isEmpty()) {
            return List.of();
        }
        List<HotelBooking> hotels = requests.stream()
                .map(r -> {
                    HotelBookingRules.validate(r.checkInDateTime(), r.checkOutDateTime());
                    HotelBooking hotel = hotelBookingMapper.toEntity(r);
                    hotel.setTrip(trip);
                    return hotel;
                })
                .toList();
        return hotelBookingRepository.saveAll(hotels);
    }

    private TripResponse assembleResponse(
            Trip trip, List<Traveler> travelers, List<FlightSegment> flights, List<HotelBooking> hotels) {
        return tripMapper.toResponse(
                trip,
                travelers.stream().map(travelerMapper::toResponse).toList(),
                flights.stream().map(flightSegmentMapper::toResponse).toList(),
                hotels.stream().map(hotelBookingMapper::toResponse).toList());
    }
}
