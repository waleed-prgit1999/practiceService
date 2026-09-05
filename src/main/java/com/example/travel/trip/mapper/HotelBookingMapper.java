package com.example.travel.trip.mapper;

import com.example.travel.trip.dto.CreateHotelBookingRequest;
import com.example.travel.trip.dto.HotelBookingResponse;
import com.example.travel.trip.dto.UpdateHotelBookingRequest;
import com.example.travel.trip.entity.HotelBooking;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface HotelBookingMapper {

    @Mapping(target = "tripId", source = "trip.id")
    HotelBookingResponse toResponse(HotelBooking hotelBooking);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "trip", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    HotelBooking toEntity(CreateHotelBookingRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "trip", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    void updateEntity(UpdateHotelBookingRequest request, @MappingTarget HotelBooking hotelBooking);
}
