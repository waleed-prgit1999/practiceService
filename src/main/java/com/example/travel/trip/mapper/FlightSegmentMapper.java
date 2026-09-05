package com.example.travel.trip.mapper;

import com.example.travel.trip.dto.CreateFlightSegmentRequest;
import com.example.travel.trip.dto.FlightSegmentResponse;
import com.example.travel.trip.dto.UpdateFlightSegmentRequest;
import com.example.travel.trip.entity.FlightSegment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface FlightSegmentMapper {

    @Mapping(target = "tripId", source = "trip.id")
    FlightSegmentResponse toResponse(FlightSegment flightSegment);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "trip", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    FlightSegment toEntity(CreateFlightSegmentRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "trip", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    void updateEntity(UpdateFlightSegmentRequest request, @MappingTarget FlightSegment flightSegment);
}
