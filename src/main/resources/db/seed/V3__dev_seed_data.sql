-- Development-only seed data. Only applied when the `dev` profile adds this location to
-- spring.flyway.locations (see application-dev.properties). Never contains real personal information.
-- Password for every seeded user is "DevPass123!" (see README, "Development Seed Data").

INSERT INTO users (first_name, last_name, email, password_hash, phone, status, created_at, updated_at)
VALUES
    ('Ada', 'Admin', 'admin@travel.local', '$2a$10$49GTFTtmslk/Q7IbsgWoYOFzhiSoi0wbkvIhXscQRBVO3hfMI6Z72', '+10000000001', 'ACTIVE', CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6)),
    ('Alice', 'Nguyen', 'alice@travel.local', '$2a$10$49GTFTtmslk/Q7IbsgWoYOFzhiSoi0wbkvIhXscQRBVO3hfMI6Z72', '+10000000002', 'ACTIVE', CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6)),
    ('Bilal', 'Khan', 'bilal@travel.local', '$2a$10$49GTFTtmslk/Q7IbsgWoYOFzhiSoi0wbkvIhXscQRBVO3hfMI6Z72', '+10000000003', 'ACTIVE', CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6)),
    ('Carla', 'Silva', 'carla@travel.local', '$2a$10$49GTFTtmslk/Q7IbsgWoYOFzhiSoi0wbkvIhXscQRBVO3hfMI6Z72', '+10000000004', 'DISABLED', CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6));

INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id FROM users u, roles r WHERE u.email = 'admin@travel.local' AND r.name = 'ADMIN';
INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id FROM users u, roles r WHERE u.email = 'alice@travel.local' AND r.name = 'USER';
INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id FROM users u, roles r WHERE u.email = 'bilal@travel.local' AND r.name = 'USER';
INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id FROM users u, roles r WHERE u.email = 'carla@travel.local' AND r.name = 'USER';

-- Alice: Paris trip with one flight and one hotel.
INSERT INTO trips (owner_id, name, description, destination, start_date_time, end_date_time, status, created_at, updated_at)
SELECT id, 'Paris getaway', 'Long weekend in Paris', 'Paris',
       '2026-09-10 09:00:00.000000', '2026-09-14 11:00:00.000000', 'PLANNED', CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6)
FROM users WHERE email = 'alice@travel.local';

INSERT INTO flight_segments (trip_id, airline, flight_number, departure_airport, arrival_airport, departure_date_time, arrival_date_time, status, created_at, updated_at)
SELECT t.id, 'Air France', 'AF123', 'JFK', 'CDG', '2026-09-10 09:00:00.000000', '2026-09-10 21:30:00.000000', 'SCHEDULED', CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6)
FROM trips t JOIN users u ON u.id = t.owner_id WHERE u.email = 'alice@travel.local' AND t.name = 'Paris getaway';

INSERT INTO hotel_bookings (trip_id, hotel_name, city, address, check_in_date_time, check_out_date_time, confirmation_number, status, created_at, updated_at)
SELECT t.id, 'Hotel Lumiere', 'Paris', '1 Rue de Rivoli', '2026-09-10 22:00:00.000000', '2026-09-14 11:00:00.000000', 'CONF-ALICE-1', 'RESERVED', CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6)
FROM trips t JOIN users u ON u.id = t.owner_id WHERE u.email = 'alice@travel.local' AND t.name = 'Paris getaway';

INSERT INTO travelers (trip_id, first_name, last_name, email, date_of_birth, passport_number, nationality, created_at, updated_at)
SELECT t.id, 'Alice', 'Nguyen', 'alice@travel.local', '1990-03-15', 'X1234567', 'US', CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6)
FROM trips t JOIN users u ON u.id = t.owner_id WHERE u.email = 'alice@travel.local' AND t.name = 'Paris getaway';

-- Alice: a second, already-completed trip (no flights/hotels attached, dates set directly).
INSERT INTO trips (owner_id, name, description, destination, start_date_time, end_date_time, status, created_at, updated_at)
SELECT id, 'Rome history tour', 'Guided tour of ancient Rome', 'Rome',
       '2026-05-01 08:00:00.000000', '2026-05-05 18:00:00.000000', 'COMPLETED', CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6)
FROM users WHERE email = 'alice@travel.local';

-- Bilal: Tokyo trip with a flight, hotel and two travelers.
INSERT INTO trips (owner_id, name, description, destination, start_date_time, end_date_time, status, created_at, updated_at)
SELECT id, 'Tokyo business trip', 'Client visit and conference', 'Tokyo',
       '2026-10-01 07:00:00.000000', '2026-10-06 15:00:00.000000', 'PLANNED', CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6)
FROM users WHERE email = 'bilal@travel.local';

INSERT INTO flight_segments (trip_id, airline, flight_number, departure_airport, arrival_airport, departure_date_time, arrival_date_time, status, created_at, updated_at)
SELECT t.id, 'ANA', 'NH105', 'SFO', 'HND', '2026-10-01 07:00:00.000000', '2026-10-02 14:20:00.000000', 'SCHEDULED', CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6)
FROM trips t JOIN users u ON u.id = t.owner_id WHERE u.email = 'bilal@travel.local' AND t.name = 'Tokyo business trip';

INSERT INTO hotel_bookings (trip_id, hotel_name, city, address, check_in_date_time, check_out_date_time, confirmation_number, status, created_at, updated_at)
SELECT t.id, 'Shinjuku Grand Hotel', 'Tokyo', '2-1 Nishi-Shinjuku', '2026-10-02 16:00:00.000000', '2026-10-06 11:00:00.000000', 'CONF-BILAL-1', 'RESERVED', CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6)
FROM trips t JOIN users u ON u.id = t.owner_id WHERE u.email = 'bilal@travel.local' AND t.name = 'Tokyo business trip';

INSERT INTO travelers (trip_id, first_name, last_name, email, date_of_birth, passport_number, nationality, created_at, updated_at)
SELECT t.id, 'Bilal', 'Khan', 'bilal@travel.local', '1988-07-22', 'Y7654321', 'CA', CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6)
FROM trips t JOIN users u ON u.id = t.owner_id WHERE u.email = 'bilal@travel.local' AND t.name = 'Tokyo business trip';
INSERT INTO travelers (trip_id, first_name, last_name, email, date_of_birth, passport_number, nationality, created_at, updated_at)
SELECT t.id, 'Priya', 'Rao', 'priya.rao@example.com', '1991-11-02', 'Z2233445', 'CA', CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6)
FROM trips t JOIN users u ON u.id = t.owner_id WHERE u.email = 'bilal@travel.local' AND t.name = 'Tokyo business trip';

-- Notifications.
INSERT INTO notifications (user_id, type, title, message, is_read, created_at)
SELECT id, 'TRIP', 'Trip created', 'Your trip ''Paris getaway'' has been created.', FALSE, CURRENT_TIMESTAMP(6)
FROM users WHERE email = 'alice@travel.local';
INSERT INTO notifications (user_id, type, title, message, is_read, created_at)
SELECT id, 'TRIP', 'Trip created', 'Your trip ''Tokyo business trip'' has been created.', FALSE, CURRENT_TIMESTAMP(6)
FROM users WHERE email = 'bilal@travel.local';
INSERT INTO notifications (user_id, type, title, message, is_read, created_at)
SELECT id, 'SYSTEM', 'Welcome to Travel Platform', 'Thanks for joining — start by creating your first trip.', TRUE, CURRENT_TIMESTAMP(6)
FROM users WHERE email = 'carla@travel.local';
