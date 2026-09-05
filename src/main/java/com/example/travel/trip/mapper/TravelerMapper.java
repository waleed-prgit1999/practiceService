package com.example.travel.trip.mapper;

import com.example.travel.trip.dto.CreateTravelerRequest;
import com.example.travel.trip.dto.TravelerResponse;
import com.example.travel.trip.dto.UpdateTravelerRequest;
import com.example.travel.trip.entity.Traveler;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface TravelerMapper {

    @Mapping(target = "tripId", source = "trip.id")
    TravelerResponse toResponse(Traveler traveler);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "trip", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    Traveler toEntity(CreateTravelerRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "trip", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    void updateEntity(UpdateTravelerRequest request, @MappingTarget Traveler traveler);
}
