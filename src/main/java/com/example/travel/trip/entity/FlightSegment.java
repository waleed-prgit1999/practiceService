package com.example.travel.trip.entity;

import com.example.travel.common.entity.SoftDeletableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "flight_segments")
@Getter
@Setter
@NoArgsConstructor
public class FlightSegment extends SoftDeletableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tripId", nullable = false)
    private Trip trip;

    @Column(nullable = false, length = 100)
    private String airline;

    @Column(nullable = false, length = 20)
    private String flightNumber;

    @Column(nullable = false, length = 10)
    private String departureAirport;

    @Column(nullable = false, length = 10)
    private String arrivalAirport;

    @Column(nullable = false)
    private Instant departureDateTime;

    @Column(nullable = false)
    private Instant arrivalDateTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private FlightStatus status = FlightStatus.SCHEDULED;
}
