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
@Table(name = "hotel_bookings")
@Getter
@Setter
@NoArgsConstructor
public class HotelBooking extends SoftDeletableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tripId", nullable = false)
    private Trip trip;

    @Column(nullable = false, length = 150)
    private String hotelName;

    @Column(nullable = false, length = 100)
    private String city;

    @Column(length = 255)
    private String address;

    @Column(nullable = false)
    private Instant checkInDateTime;

    @Column(nullable = false)
    private Instant checkOutDateTime;

    @Column(length = 50)
    private String confirmationNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private HotelStatus status = HotelStatus.RESERVED;
}
