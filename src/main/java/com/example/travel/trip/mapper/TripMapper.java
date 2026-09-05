package com.example.travel.trip.mapper;

import com.example.travel.trip.dto.CreateTripRequest;
import com.example.travel.trip.dto.FlightSegmentResponse;
import com.example.travel.trip.dto.HotelBookingResponse;
import com.example.travel.trip.dto.TravelerResponse;
import com.example.travel.trip.dto.TripResponse;
import com.example.travel.trip.dto.TripSummaryResponse;
import com.example.travel.trip.dto.UpdateTripRequest;
import com.example.travel.trip.entity.Trip;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface TripMapper {

    TripSummaryResponse toSummary(Trip trip);

    @Mapping(target = "travelers", source = "travelers")
    @Mapping(target = "flights", source = "flights")
    @Mapping(target = "hotels", source = "hotels")
    TripResponse toResponse(
            Trip trip,
            List<TravelerResponse> travelers,
            List<FlightSegmentResponse> flights,
            List<HotelBookingResponse> hotels);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "ownerId", ignore = true)
    @Mapping(target = "startDateTime", ignore = true)
    @Mapping(target = "endDateTime", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    Trip toEntity(CreateTripRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "ownerId", ignore = true)
    @Mapping(target = "startDateTime", ignore = true)
    @Mapping(target = "endDateTime", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    void updateEntity(UpdateTripRequest request, @MappingTarget Trip trip);
}
