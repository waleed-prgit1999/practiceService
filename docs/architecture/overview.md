# Architecture Overview

## Request flow

```
Client
  |
  v
Spring Security + JWT   (JwtAuthenticationFilter, SecurityConfig)
  |
  v
Controller               (validates input, maps DTOs, never touches repositories)
  |
  v
Service                  (business rules, ownership checks, @Transactional boundary)
  |
  v
Repository                (Spring Data JPA)
  |
  v
MySQL
```

Every request that isn't under `/api/v1/auth/**` (or Swagger/actuator) passes through
`JwtAuthenticationFilter`, which validates the bearer access token and populates the security
context from its claims — no database lookup on the hot path. Controllers depend only on services;
services depend only on repositories and other services; repositories are the only layer that
issues queries. Entities never cross the controller boundary — every response is a DTO produced by
a MapStruct mapper.

## Modules

```
com.example.travel
├── common/         cross-cutting: error handling, pagination, JPA auditing base
                     classes, the event-publisher port
├── auth/            Spring Security config, JWT issuing/parsing, register/login/refresh
├── user/            User + Role entities, profile self-service, admin user management
├── trip/            Trip, Traveler, FlightSegment, HotelBooking; the trip search port
├── notification/     notification persistence and retrieval
└── audit/            AuditService + audit trail retrieval
```

Each module owns its own `controller/service/repository/entity/dto/mapper` slice. Cross-module
calls flow one way — `trip` depends on `user` (to check account status) and `notification` (to
raise a "trip created" notification); `auth` depends on `user`. Nothing depends back on `trip`,
`auth`, or `notification`, so those modules could become separate services later without their
dependents needing to change.

## Ownership and access control

Role-based checks (e.g. "must be ADMIN") are enforced declaratively with
`@PreAuthorize`/`@EnableMethodSecurity` at the controller layer. Resource-ownership checks (e.g.
"a user may only see or modify their own trips") are a business rule, not an authentication
concern, so they live in the service layer — `TripAccessGuard` loads a trip and throws
`ForbiddenOperationException` (403) if the caller neither owns it nor is an admin. The same guard
enforces that cancelled trips can't be mutated further.

## The two extension seams

**Search port** (`trip.search.TripSearchService`): `MySqlTripSearchService` builds a Spring Data
`Specification` from `TripSearchCriteria`. Controllers and `TripService` depend only on the
interface. A future `ElasticsearchTripSearchService` implements the same contract.

**Event port** (`common.event.DomainEventPublisher`): `LoggingDomainEventPublisher` just logs.
`AuthService`, `TripService`, `FlightSegmentService`, `HotelBookingService` and
`NotificationService` publish domain events (`UserRegisteredEvent`, `TripCreatedEvent`, etc.)
through this interface without knowing what happens to them. A future
`KafkaDomainEventPublisher`/`RabbitMqEventPublisher` replaces the logging implementation only —
no caller changes.

## Future extensions

```
                    +-- Redis            (Phase 2: caching)
                    |
Spring Boot --------+-- Elasticsearch    (Phase 3: TripSearchService impl)
                    |
                    +-- RabbitMQ / Kafka (Phase 5: DomainEventPublisher impl)
                    |
                    +-- Microservices    (Phase 6: module -> service extraction)
```

See [decisions.md](decisions.md) for the reasoning behind each of these choices.
