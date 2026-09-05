-- =====================================================================================
-- Travel Management Platform — full MySQL schema (Phase 1)
-- =====================================================================================
-- Standalone, consolidated equivalent of the Flyway migrations:
--   src/main/resources/db/migration/V1__init_schema.sql
--   src/main/resources/db/migration/V2__seed_reference_roles.sql
--   src/main/resources/db/migration/V4__rename_columns_to_camel_case.sql
--
-- This file reflects the CURRENT shape of the schema, i.e. the result of running all of
-- the above. It creates the columns already camelCase rather than creating them in
-- snake_case and renaming them.
--
-- Use it for inspecting the schema, provisioning a database by hand, or seeding a
-- non-Flyway environment.
--
-- Naming convention
--   Columns are camelCase and match the Java field names exactly (Hibernate uses
--   PhysicalNamingStrategyStandardImpl, so field names pass straight through).
--   Tables stay snake_case on purpose: MySQL folds table names to lowercase on Windows
--   (lower_case_table_names=1 by default) but preserves case on Linux, so camelCase table
--   names would work in the Docker container and break against a native Windows MySQL.
--   Column names are unaffected — they keep their case and compare case-insensitively.
--
-- !! IMPORTANT !!
-- The application manages its own schema through Flyway. If you run this script by hand
-- and then start the app against the same database, Flyway will fail: the tables exist
-- but its `flyway_schema_history` table does not. Pick ONE of:
--   a) Let the app create everything  -> don't run this script; just start the app.
--   b) Run this script yourself        -> then set spring.flyway.baseline-on-migrate=true
--                                         and spring.flyway.baseline-version=4, so Flyway
--                                         adopts the existing schema instead of replaying
--                                         V1-V4.
--
-- Engine/charset: InnoDB + utf8mb4 throughout. All timestamps are DATETIME(6) and are
-- stored in UTC (spring.jpa.properties.hibernate.jdbc.time_zone=UTC).
-- IDs are BIGINT AUTO_INCREMENT — see docs/architecture/decisions.md, "ID Strategy".
-- =====================================================================================

CREATE DATABASE IF NOT EXISTS travel_platform
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_0900_ai_ci;

USE travel_platform;


-- -------------------------------------------------------------------------------------
-- users
-- Application accounts. Passwords are stored only as BCrypt hashes, never plaintext.
-- status: 'ACTIVE' | 'DISABLED'   (com.example.travel.user.entity.UserStatus)
-- Email is normalized to lowercase by the service layer before insert.
-- -------------------------------------------------------------------------------------
CREATE TABLE users (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    firstName     VARCHAR(100)   NOT NULL,
    lastName      VARCHAR(100)   NOT NULL,
    email         VARCHAR(255)   NOT NULL,
    passwordHash  VARCHAR(255)   NOT NULL,
    phone         VARCHAR(30),
    status        VARCHAR(20)    NOT NULL,
    createdAt     DATETIME(6)    NOT NULL,
    updatedAt     DATETIME(6)    NOT NULL,
    CONSTRAINT uq_users_email UNIQUE (email)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE INDEX idx_users_status ON users (status);


-- -------------------------------------------------------------------------------------
-- roles
-- Reference data. Rows are inserted at the bottom of this script.
-- name: 'USER' | 'ADMIN'          (com.example.travel.user.entity.RoleName)
-- -------------------------------------------------------------------------------------
CREATE TABLE roles (
    id    BIGINT AUTO_INCREMENT PRIMARY KEY,
    name  VARCHAR(50) NOT NULL,
    CONSTRAINT uq_roles_name UNIQUE (name)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;


-- -------------------------------------------------------------------------------------
-- user_roles
-- Many-to-many join between users and roles. Composite primary key, cascading deletes
-- on both sides (removing a user or a role removes the assignment, not the counterpart).
-- -------------------------------------------------------------------------------------
CREATE TABLE user_roles (
    userId  BIGINT NOT NULL,
    roleId  BIGINT NOT NULL,
    PRIMARY KEY (userId, roleId),
    CONSTRAINT fk_user_roles_user FOREIGN KEY (userId) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_user_roles_role FOREIGN KEY (roleId) REFERENCES roles (id) ON DELETE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;


-- -------------------------------------------------------------------------------------
-- trips
-- status: 'PLANNED' | 'ONGOING' | 'COMPLETED' | 'CANCELLED'
--                                 (com.example.travel.trip.entity.TripStatus)
-- deletedAt: soft delete. NULL = live; non-NULL = deleted. All reads filter on IS NULL.
-- startDateTime / endDateTime are DERIVED, never client-supplied: TripService recomputes
-- them as min(flight departure, hotel check-in) / max(flight arrival, hotel check-out)
-- across the trip's non-deleted children. Both are NULL until the trip has at least one
-- flight or hotel.
-- -------------------------------------------------------------------------------------
CREATE TABLE trips (
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    ownerId        BIGINT         NOT NULL,
    name           VARCHAR(150)   NOT NULL,
    description    VARCHAR(2000),
    destination    VARCHAR(150)   NOT NULL,
    startDateTime  DATETIME(6),
    endDateTime    DATETIME(6),
    status         VARCHAR(20)    NOT NULL,
    createdAt      DATETIME(6)    NOT NULL,
    updatedAt      DATETIME(6)    NOT NULL,
    deletedAt      DATETIME(6),
    CONSTRAINT fk_trips_owner FOREIGN KEY (ownerId) REFERENCES users (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE INDEX idx_trips_ownerId ON trips (ownerId);
CREATE INDEX idx_trips_status ON trips (status);
CREATE INDEX idx_trips_startDateTime ON trips (startDateTime);
CREATE INDEX idx_trips_destination ON trips (destination);


-- -------------------------------------------------------------------------------------
-- travelers
-- People travelling on a trip. Identity verification is intentionally out of scope in
-- Phase 1, so passportNumber / nationality are optional and unvalidated.
-- -------------------------------------------------------------------------------------
CREATE TABLE travelers (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    tripId          BIGINT        NOT NULL,
    firstName       VARCHAR(100)  NOT NULL,
    lastName        VARCHAR(100)  NOT NULL,
    email           VARCHAR(255)  NOT NULL,
    dateOfBirth     DATE          NOT NULL,
    passportNumber  VARCHAR(50),
    nationality     VARCHAR(100),
    createdAt       DATETIME(6)   NOT NULL,
    updatedAt       DATETIME(6)   NOT NULL,
    deletedAt       DATETIME(6),
    CONSTRAINT fk_travelers_trip FOREIGN KEY (tripId) REFERENCES trips (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE INDEX idx_travelers_tripId ON travelers (tripId);


-- -------------------------------------------------------------------------------------
-- flight_segments
-- status: 'SCHEDULED' | 'DELAYED' | 'CANCELLED' | 'COMPLETED'
--                                 (com.example.travel.trip.entity.FlightStatus)
-- Business rules enforced in the service layer (FlightSegmentRules), not by constraints:
--   - arrivalAirport must differ from departureAirport
--   - arrivalDateTime must be after departureDateTime
-- -------------------------------------------------------------------------------------
CREATE TABLE flight_segments (
    id                 BIGINT AUTO_INCREMENT PRIMARY KEY,
    tripId             BIGINT        NOT NULL,
    airline            VARCHAR(100)  NOT NULL,
    flightNumber       VARCHAR(20)   NOT NULL,
    departureAirport   VARCHAR(10)   NOT NULL,
    arrivalAirport     VARCHAR(10)   NOT NULL,
    departureDateTime  DATETIME(6)   NOT NULL,
    arrivalDateTime    DATETIME(6)   NOT NULL,
    status             VARCHAR(20)   NOT NULL,
    createdAt          DATETIME(6)   NOT NULL,
    updatedAt          DATETIME(6)   NOT NULL,
    deletedAt          DATETIME(6),
    CONSTRAINT fk_flight_segments_trip FOREIGN KEY (tripId) REFERENCES trips (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE INDEX idx_flight_segments_tripId ON flight_segments (tripId);


-- -------------------------------------------------------------------------------------
-- hotel_bookings
-- status: 'RESERVED' | 'CANCELLED' | 'COMPLETED'
--                                 (com.example.travel.trip.entity.HotelStatus)
-- Business rule enforced in the service layer (HotelBookingRules):
--   - checkOutDateTime must be after checkInDateTime
-- -------------------------------------------------------------------------------------
CREATE TABLE hotel_bookings (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    tripId              BIGINT        NOT NULL,
    hotelName           VARCHAR(150)  NOT NULL,
    city                VARCHAR(100)  NOT NULL,
    address             VARCHAR(255),
    checkInDateTime     DATETIME(6)   NOT NULL,
    checkOutDateTime    DATETIME(6)   NOT NULL,
    confirmationNumber  VARCHAR(50),
    status              VARCHAR(20)   NOT NULL,
    createdAt           DATETIME(6)   NOT NULL,
    updatedAt           DATETIME(6)   NOT NULL,
    deletedAt           DATETIME(6),
    CONSTRAINT fk_hotel_bookings_trip FOREIGN KEY (tripId) REFERENCES trips (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE INDEX idx_hotel_bookings_tripId ON hotel_bookings (tripId);


-- -------------------------------------------------------------------------------------
-- notifications
-- type: 'SYSTEM' | 'TRIP' | 'SECURITY'
--                                 (com.example.travel.notification.entity.NotificationType)
-- Phase 1 only stores notifications; there is no email/SMS delivery.
-- The column is isRead, not read: READ is a reserved word in MySQL and would otherwise
-- need backtick-quoting in every query.
-- -------------------------------------------------------------------------------------
CREATE TABLE notifications (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    userId     BIGINT        NOT NULL,
    type       VARCHAR(20)   NOT NULL,
    title      VARCHAR(150)  NOT NULL,
    message    VARCHAR(1000) NOT NULL,
    isRead     BOOLEAN       NOT NULL DEFAULT FALSE,
    createdAt  DATETIME(6)   NOT NULL,
    CONSTRAINT fk_notifications_user FOREIGN KEY (userId) REFERENCES users (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE INDEX idx_notifications_userId_isRead ON notifications (userId, isRead);


-- -------------------------------------------------------------------------------------
-- audit_logs
-- userId is nullable (ON DELETE SET NULL) so an audit trail survives account deletion
-- and can record actions with no authenticated actor (e.g. a failed login for an unknown
-- email).
-- action: USER_REGISTERED, LOGIN_SUCCESS, LOGIN_FAILED, USER_STATUS_CHANGED,
--         TRIP_CREATED, TRIP_UPDATED, TRIP_CANCELLED,
--         TRAVELER_ADDED/UPDATED/REMOVED, FLIGHT_ADDED/UPDATED/REMOVED,
--         HOTEL_ADDED/UPDATED/REMOVED   (com.example.travel.audit.entity.AuditAction)
-- metadata: free-form JSON context, mapped via Hibernate @JdbcTypeCode(SqlTypes.JSON).
-- -------------------------------------------------------------------------------------
CREATE TABLE audit_logs (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    userId      BIGINT,
    action      VARCHAR(50)   NOT NULL,
    entityType  VARCHAR(50)   NOT NULL,
    entityId    BIGINT,
    metadata    JSON,
    createdAt   DATETIME(6)   NOT NULL,
    CONSTRAINT fk_audit_logs_user FOREIGN KEY (userId) REFERENCES users (id) ON DELETE SET NULL
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE INDEX idx_audit_logs_userId ON audit_logs (userId);
CREATE INDEX idx_audit_logs_entityType_entityId ON audit_logs (entityType, entityId);
CREATE INDEX idx_audit_logs_createdAt ON audit_logs (createdAt);


-- -------------------------------------------------------------------------------------
-- Reference data (required in every environment — the app resolves the USER role by name
-- when registering an account, and fails fast if it is missing).
-- -------------------------------------------------------------------------------------
INSERT INTO roles (name) VALUES ('USER'), ('ADMIN');


-- =====================================================================================
-- Teardown (drop everything, child tables first). Commented out on purpose.
-- =====================================================================================
-- DROP TABLE IF EXISTS audit_logs;
-- DROP TABLE IF EXISTS notifications;
-- DROP TABLE IF EXISTS hotel_bookings;
-- DROP TABLE IF EXISTS flight_segments;
-- DROP TABLE IF EXISTS travelers;
-- DROP TABLE IF EXISTS trips;
-- DROP TABLE IF EXISTS user_roles;
-- DROP TABLE IF EXISTS roles;
-- DROP TABLE IF EXISTS users;
-- DROP TABLE IF EXISTS flyway_schema_history;
