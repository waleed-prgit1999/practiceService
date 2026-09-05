package com.example.travel.trip.entity;

import com.example.travel.common.entity.SoftDeletableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "trips")
@Getter
@Setter
@NoArgsConstructor
public class Trip extends SoftDeletableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Raw id rather than a JPA relationship: trip/ deliberately doesn't depend on the user/
    // module's entity, matching AuditLog's ownerId/userId pattern elsewhere in the codebase.
    @Column(nullable = false)
    private Long ownerId;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(length = 2000)
    private String description;

    @Column(nullable = false, length = 150)
    private String destination;

    // Derived from the trip's flight segments and hotel bookings; see TripService.
    private Instant startDateTime;

    private Instant endDateTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TripStatus status = TripStatus.PLANNED;
}
