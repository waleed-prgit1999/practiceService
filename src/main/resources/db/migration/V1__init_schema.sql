-- Core schema for Phase 1: users, roles, trips and their sub-resources, notifications, audit log.
-- IDs are BIGINT AUTO_INCREMENT (see docs/architecture/decisions.md, "ID Strategy").

CREATE TABLE users (
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    first_name     VARCHAR(100)   NOT NULL,
    last_name      VARCHAR(100)   NOT NULL,
    email          VARCHAR(255)   NOT NULL,
    password_hash  VARCHAR(255)   NOT NULL,
    phone          VARCHAR(30),
    status         VARCHAR(20)    NOT NULL,
    created_at     DATETIME(6)    NOT NULL,
    updated_at     DATETIME(6)    NOT NULL,
    CONSTRAINT uq_users_email UNIQUE (email)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE INDEX idx_users_status ON users (status);

CREATE TABLE roles (
    id    BIGINT AUTO_INCREMENT PRIMARY KEY,
    name  VARCHAR(50) NOT NULL,
    CONSTRAINT uq_roles_name UNIQUE (name)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE TABLE user_roles (
    user_id  BIGINT NOT NULL,
    role_id  BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id),
    CONSTRAINT fk_user_roles_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_user_roles_role FOREIGN KEY (role_id) REFERENCES roles (id) ON DELETE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE TABLE trips (
    id                BIGINT AUTO_INCREMENT PRIMARY KEY,
    owner_id          BIGINT         NOT NULL,
    name              VARCHAR(150)   NOT NULL,
    description       VARCHAR(2000),
    destination       VARCHAR(150)   NOT NULL,
    start_date_time   DATETIME(6),
    end_date_time     DATETIME(6),
    status            VARCHAR(20)    NOT NULL,
    created_at        DATETIME(6)    NOT NULL,
    updated_at        DATETIME(6)    NOT NULL,
    deleted_at        DATETIME(6),
    CONSTRAINT fk_trips_owner FOREIGN KEY (owner_id) REFERENCES users (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE INDEX idx_trips_owner_id ON trips (owner_id);
CREATE INDEX idx_trips_status ON trips (status);
CREATE INDEX idx_trips_start_date_time ON trips (start_date_time);
CREATE INDEX idx_trips_destination ON trips (destination);

CREATE TABLE travelers (
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    trip_id          BIGINT        NOT NULL,
    first_name       VARCHAR(100)  NOT NULL,
    last_name        VARCHAR(100)  NOT NULL,
    email            VARCHAR(255)  NOT NULL,
    date_of_birth    DATE          NOT NULL,
    passport_number  VARCHAR(50),
    nationality      VARCHAR(100),
    created_at       DATETIME(6)   NOT NULL,
    updated_at       DATETIME(6)   NOT NULL,
    deleted_at       DATETIME(6),
    CONSTRAINT fk_travelers_trip FOREIGN KEY (trip_id) REFERENCES trips (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE INDEX idx_travelers_trip_id ON travelers (trip_id);

CREATE TABLE flight_segments (
    id                    BIGINT AUTO_INCREMENT PRIMARY KEY,
    trip_id               BIGINT        NOT NULL,
    airline               VARCHAR(100)  NOT NULL,
    flight_number         VARCHAR(20)   NOT NULL,
    departure_airport     VARCHAR(10)   NOT NULL,
    arrival_airport       VARCHAR(10)   NOT NULL,
    departure_date_time   DATETIME(6)   NOT NULL,
    arrival_date_time     DATETIME(6)   NOT NULL,
    status                VARCHAR(20)   NOT NULL,
    created_at            DATETIME(6)   NOT NULL,
    updated_at            DATETIME(6)   NOT NULL,
    deleted_at            DATETIME(6),
    CONSTRAINT fk_flight_segments_trip FOREIGN KEY (trip_id) REFERENCES trips (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE INDEX idx_flight_segments_trip_id ON flight_segments (trip_id);

CREATE TABLE hotel_bookings (
    id                     BIGINT AUTO_INCREMENT PRIMARY KEY,
    trip_id                BIGINT        NOT NULL,
    hotel_name             VARCHAR(150)  NOT NULL,
    city                   VARCHAR(100)  NOT NULL,
    address                VARCHAR(255),
    check_in_date_time     DATETIME(6)   NOT NULL,
    check_out_date_time    DATETIME(6)   NOT NULL,
    confirmation_number    VARCHAR(50),
    status                 VARCHAR(20)   NOT NULL,
    created_at             DATETIME(6)   NOT NULL,
    updated_at             DATETIME(6)   NOT NULL,
    deleted_at             DATETIME(6),
    CONSTRAINT fk_hotel_bookings_trip FOREIGN KEY (trip_id) REFERENCES trips (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE INDEX idx_hotel_bookings_trip_id ON hotel_bookings (trip_id);

CREATE TABLE notifications (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id     BIGINT        NOT NULL,
    type        VARCHAR(20)   NOT NULL,
    title       VARCHAR(150)  NOT NULL,
    message     VARCHAR(1000) NOT NULL,
    is_read     BOOLEAN       NOT NULL DEFAULT FALSE,
    created_at  DATETIME(6)   NOT NULL,
    CONSTRAINT fk_notifications_user FOREIGN KEY (user_id) REFERENCES users (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE INDEX idx_notifications_user_id_is_read ON notifications (user_id, is_read);

CREATE TABLE audit_logs (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id      BIGINT,
    action       VARCHAR(50)   NOT NULL,
    entity_type  VARCHAR(50)   NOT NULL,
    entity_id    BIGINT,
    metadata     JSON,
    created_at   DATETIME(6)   NOT NULL,
    CONSTRAINT fk_audit_logs_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE SET NULL
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE INDEX idx_audit_logs_user_id ON audit_logs (user_id);
CREATE INDEX idx_audit_logs_entity_type_entity_id ON audit_logs (entity_type, entity_id);
CREATE INDEX idx_audit_logs_created_at ON audit_logs (created_at);
