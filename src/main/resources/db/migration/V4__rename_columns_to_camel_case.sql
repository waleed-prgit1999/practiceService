-- Renames every multi-word column from snake_case to camelCase, so column names match the
-- Java field names exactly (Hibernate is configured with PhysicalNamingStrategyStandardImpl,
-- which maps field names straight through).
--
-- Table names are deliberately NOT renamed: MySQL folds table names to lowercase on Windows
-- (lower_case_table_names=1 by default) but preserves case on Linux, so camelCase table names
-- would work in the Docker container and break against a native Windows MySQL. Column names
-- have no such problem — they preserve their stored case and compare case-insensitively.
--
-- Requires MySQL 8.0+ for ALTER TABLE ... RENAME COLUMN / RENAME INDEX (project targets 8.4).
--
-- Note on ordering: V3__dev_seed_data.sql runs before this migration and therefore inserts
-- into the original snake_case columns. That is correct — it reflects the schema as it existed
-- at that point in history. Migrations are immutable; only the schema moves forward.
--
-- MySQL does not roll DDL back (each statement implicitly commits), so a failure part-way
-- through leaves the database partially renamed. Run against a disposable or backed-up
-- database.

-- -------------------------------------------------------------------------------------
-- Columns
-- -------------------------------------------------------------------------------------

ALTER TABLE users
    RENAME COLUMN first_name    TO firstName,
    RENAME COLUMN last_name     TO lastName,
    RENAME COLUMN password_hash TO passwordHash,
    RENAME COLUMN created_at    TO createdAt,
    RENAME COLUMN updated_at    TO updatedAt;

ALTER TABLE user_roles
    RENAME COLUMN user_id TO userId,
    RENAME COLUMN role_id TO roleId;

ALTER TABLE trips
    RENAME COLUMN owner_id        TO ownerId,
    RENAME COLUMN start_date_time TO startDateTime,
    RENAME COLUMN end_date_time   TO endDateTime,
    RENAME COLUMN created_at      TO createdAt,
    RENAME COLUMN updated_at      TO updatedAt,
    RENAME COLUMN deleted_at      TO deletedAt;

ALTER TABLE travelers
    RENAME COLUMN trip_id         TO tripId,
    RENAME COLUMN first_name      TO firstName,
    RENAME COLUMN last_name       TO lastName,
    RENAME COLUMN date_of_birth   TO dateOfBirth,
    RENAME COLUMN passport_number TO passportNumber,
    RENAME COLUMN created_at      TO createdAt,
    RENAME COLUMN updated_at      TO updatedAt,
    RENAME COLUMN deleted_at      TO deletedAt;

ALTER TABLE flight_segments
    RENAME COLUMN trip_id             TO tripId,
    RENAME COLUMN flight_number       TO flightNumber,
    RENAME COLUMN departure_airport   TO departureAirport,
    RENAME COLUMN arrival_airport     TO arrivalAirport,
    RENAME COLUMN departure_date_time TO departureDateTime,
    RENAME COLUMN arrival_date_time   TO arrivalDateTime,
    RENAME COLUMN created_at          TO createdAt,
    RENAME COLUMN updated_at          TO updatedAt,
    RENAME COLUMN deleted_at          TO deletedAt;

ALTER TABLE hotel_bookings
    RENAME COLUMN trip_id             TO tripId,
    RENAME COLUMN hotel_name          TO hotelName,
    RENAME COLUMN check_in_date_time  TO checkInDateTime,
    RENAME COLUMN check_out_date_time TO checkOutDateTime,
    RENAME COLUMN confirmation_number TO confirmationNumber,
    RENAME COLUMN created_at          TO createdAt,
    RENAME COLUMN updated_at          TO updatedAt,
    RENAME COLUMN deleted_at          TO deletedAt;

-- is_read becomes isRead, not `read`: READ is a reserved word in MySQL and would need
-- backtick-quoting everywhere it appears.
ALTER TABLE notifications
    RENAME COLUMN user_id    TO userId,
    RENAME COLUMN is_read    TO isRead,
    RENAME COLUMN created_at TO createdAt;

ALTER TABLE audit_logs
    RENAME COLUMN user_id     TO userId,
    RENAME COLUMN entity_type TO entityType,
    RENAME COLUMN entity_id   TO entityId,
    RENAME COLUMN created_at  TO createdAt;

-- -------------------------------------------------------------------------------------
-- Indexes whose names embed a renamed column. Metadata-only; the index definitions
-- themselves already follow the renamed columns.
-- Indexes over columns that did not change (idx_users_status, idx_trips_status,
-- idx_trips_destination) keep their names.
-- -------------------------------------------------------------------------------------

ALTER TABLE trips
    RENAME INDEX idx_trips_owner_id        TO idx_trips_ownerId,
    RENAME INDEX idx_trips_start_date_time TO idx_trips_startDateTime;

ALTER TABLE travelers
    RENAME INDEX idx_travelers_trip_id TO idx_travelers_tripId;

ALTER TABLE flight_segments
    RENAME INDEX idx_flight_segments_trip_id TO idx_flight_segments_tripId;

ALTER TABLE hotel_bookings
    RENAME INDEX idx_hotel_bookings_trip_id TO idx_hotel_bookings_tripId;

ALTER TABLE notifications
    RENAME INDEX idx_notifications_user_id_is_read TO idx_notifications_userId_isRead;

ALTER TABLE audit_logs
    RENAME INDEX idx_audit_logs_user_id                 TO idx_audit_logs_userId,
    RENAME INDEX idx_audit_logs_entity_type_entity_id   TO idx_audit_logs_entityType_entityId,
    RENAME INDEX idx_audit_logs_created_at              TO idx_audit_logs_createdAt;
