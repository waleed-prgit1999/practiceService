# Claude Code Plan — Phase 1: Travel Management Platform
## Spring Boot + MySQL Modular Monolith

## 1. Objective

Build Phase 1 of a production-style Travel Management Platform as a modular monolith using:

- Java 21+
- Spring Boot 3.x
- Maven
- Spring Web
- Spring Data JPA
- Hibernate
- MySQL 8+
- Bean Validation
- Spring Security
- JWT authentication
- MapStruct
- Lombok
- Flyway
- OpenAPI / Swagger
- JUnit 5
- Mockito
- Testcontainers where appropriate

The architecture must deliberately leave clean extension points for future phases involving:

- Redis
- Elasticsearch
- RabbitMQ
- Apache Kafka
- Docker / Docker Compose
- API Gateway
- Microservices
- Observability
- CI/CD

Do NOT implement those technologies in Phase 1. Design the code so they can be introduced later without rewriting business logic.

---

# 2. Product Scope

The application is a backend for managing users and their travel plans.

A registered user can:

1. Register and authenticate.
2. View/update their profile.
3. Create trips.
4. Update/cancel trips.
5. Add travelers to trips.
6. Add flight segments.
7. Add hotel bookings.
8. Add activities.
9. View a complete trip itinerary.
10. Search/filter/paginate their trips.
11. View notifications.
12. View an audit trail of important actions.

An administrator can:

1. View users.
2. Enable/disable users.
3. View all trips.
4. View system-level audit information.

Keep the initial domain intentionally manageable. Do not implement real airline/hotel integrations or payment processing.

---

# 3. Architecture

Use a modular monolith.

Recommended structure:

travel-platform/
├── pom.xml
├── README.md
├── docker-compose.yml
├── .gitignore
├── docs/
│   ├── architecture/
│   ├── api/
│   └── database/
└── src/
    ├── main/
    │   ├── java/com/example/travel/
    │   │   ├── TravelApplication.java
    │   │   ├── common/
    │   │   ├── auth/
    │   │   ├── user/
    │   │   ├── trip/
    │   │   ├── notification/
    │   │   └── audit/
    │   └── resources/
    │       ├── application.yml
    │       └── db/migration/
    └── test/

Within each feature module, prefer:

module/
├── controller/
├── service/
├── repository/
├── entity/
├── dto/
├── mapper/
├── exception/
└── validation/

Do not create a giant global controller/service/repository package.

---

# 4. Architectural Rules

## 4.1 Controllers

Controllers must:

- Accept HTTP requests.
- Validate input.
- Call application/service layer.
- Return DTOs.
- Never contain business logic.
- Never directly access repositories.

## 4.2 Services

Services contain business rules.

Example:

TripController
    -> TripService
        -> TripRepository

Never:

TripController
    -> TripRepository

## 4.3 Entities

JPA entities are persistence models.

Do not expose entities directly from REST endpoints.

Use:

Entity -> Mapper -> Response DTO

and:

Request DTO -> Mapper -> Entity

## 4.4 DTOs

Create separate request and response DTOs.

Examples:

CreateUserRequest
UpdateUserRequest
UserResponse

CreateTripRequest
UpdateTripRequest
TripResponse

Do not reuse entity classes as request objects.

## 4.5 Database access

Use Spring Data JPA.

Do not use native SQL unless there is a demonstrated need.

For complex filtering, use Spring Data Specifications or a clean query abstraction.

## 4.6 Transactions

Use @Transactional at service/application boundaries.

Avoid placing @Transactional blindly on every method.

## 4.7 Error handling

Implement a global exception handler using @RestControllerAdvice.

Return a consistent error structure.

Example:

{
  "timestamp": "...",
  "status": 400,
  "error": "VALIDATION_ERROR",
  "message": "Invalid request",
  "path": "/api/trips",
  "details": [...]
}

Do not expose stack traces.

---

# 5. Domain Model

Implement the following initial entities.

## 5.1 User

Fields:

- id
- firstName
- lastName
- email
- passwordHash
- phone
- status
- createdAt
- updatedAt

Status:

ACTIVE
DISABLED

Constraints:

- Email unique.
- Email normalized to lowercase.
- Password must never be returned by APIs.

## 5.2 Role

Fields:

- id
- name

Roles:

USER
ADMIN

Create a many-to-many relationship between users and roles.

## 5.3 Trip

Fields:

- id
- userId / owner
- name
- description
- destination
- startDateTime
- endDateTime
- status
- createdAt
- updatedAt
- deledtedAt

Statuses:

PLANNED
ONGOING
COMPLETED
CANCELLED

Rules:

- End date cannot be before start date.
- Disabled users cannot create new trips.
- A user can only modify their own trips unless they are ADMIN.
- Cancelled trips cannot be modified except where explicitly allowed.
- startDateTime and endDate will be calculated based on the flightSegments and hotelBooking of that trip i.e. startDateTime will be start of earliest flightSegment/hotelBooking and endDate will be end of lastest flightSegment/hotelBooking

## 5.4 Traveler

Fields:

- id
- tripId
- firstName
- lastName
- email
- dateOfBirth
- passportNumber (optional)
- nationality (optional)
- createdAt
- updatedAt
- deledtedAt

Do not over-engineer identity verification in Phase 1.

## 5.5 FlightSegment

Fields:

- id
- tripId
- airline
- flightNumber
- departureAirport
- arrivalAirport
- departureDateTime
- arrivalDateTime
- status
- createdAt
- updatedAt
- deledtedAt

Statuses:

SCHEDULED
DELAYED
CANCELLED
COMPLETED

Validate:

- Arrival airport differs from departure airport.
- Arrival time is after departure time.

## 5.6 HotelBooking

Fields:

- id
- tripId
- hotelName
- city
- address
- checkInDateTime
- checkOutCheckOutDateTime
- confirmationNumber
- status
- createdAt
- updatedAt
- deledtedAt

Statuses:

RESERVED
CANCELLED
COMPLETED

Validate:

- Check-out is after check-in.

## 5.8 Notification

Fields:

- id
- userId
- type
- title
- message
- read
- createdAt

Types:

SYSTEM
TRIP
SECURITY

Phase 1 only stores notifications.

Do not implement email/SMS delivery yet.

## 5.9 AuditLog

Fields:

- id
- userId nullable
- action
- entityType
- entityId
- metadata
- createdAt

Examples:

USER_REGISTERED
LOGIN_SUCCESS
LOGIN_FAILED
TRIP_CREATED
TRIP_UPDATED
TRIP_CANCELLED
TRAVELER_ADDED
FLIGHT_ADDED

The audit system should be designed so that it can later publish events to Kafka/RabbitMQ.

---

# 6. Database Design

Use MySQL.

Recommended tables:

users
roles
user_roles
trips
travelers
flight_segments
hotel_bookings
notifications
audit_logs

Use:

- BIGINT or UUID consistently.
- UTC timestamps.
- Foreign keys.
- Unique constraints.
- Indexes on common lookup columns.

Recommended initial indexes:

users(email)
users(status)

trips(owner_id)
trips(status)
trips(start_date)
trips(destination)

travelers(trip_id)
flight_segments(trip_id)
hotel_bookings(trip_id)
activities(trip_id)

notifications(user_id, read)
audit_logs(user_id)
audit_logs(entity_type, entity_id)
audit_logs(created_at)

Do not create unnecessary indexes.

---

# 7. ID Strategy

Use UUIDs for public-facing identifiers if practical.

Do not expose database implementation details unnecessarily.

If UUID is selected:

- Store UUID using a MySQL-compatible efficient representation.
- Use a consistent Java UUID type.
- Never mix Long IDs and UUID IDs arbitrarily.

If implementation simplicity is prioritized, use BIGINT IDs initially but hide them behind DTOs and repository boundaries.

Document the decision in docs/architecture/decisions.md.

---

# 9. API Design

Base URL:

/api/v1

## Authentication

POST /api/v1/auth/register
POST /api/v1/auth/login
POST /api/v1/auth/refresh

## Users

GET /api/v1/users/me
PUT /api/v1/users/me

ADMIN:

GET /api/v1/users
GET /api/v1/users/{id}
PATCH /api/v1/users/{id}/status

## Trips

POST /api/v1/trips -- will take list of flights and hotels and save them along with trip
GET /api/v1/trips
GET /api/v1/trips/{id} -- will give lists of flights and hotels associated with that trip
PUT /api/v1/trips/{id}
DELETE /api/v1/trips/{id} -- will delete all the associated flights and hotels as well. Deleting means soft delete i.e. deletedAt is set 

Filtering:

GET /api/v1/trips?status=PLANNED
GET /api/v1/trips?destination=London
GET /api/v1/trips?from=2026-09-01&to=2026-12-31
GET /api/v1/trips?page=0&size=20&sort=startDate,asc

## Travelers

POST /api/v1/trips/{tripId}/travelers
GET /api/v1/trips/{tripId}/travelers
PUT /api/v1/trips/{tripId}/travelers/{travelerId}
DELETE /api/v1/trips/{tripId}/travelers/{travelerId} -- Deleting means soft delete i.e. deletedAt is set 

## Flights

POST /api/v1/trips/{tripId}/flights
GET /api/v1/trips/{tripId}/flights
PUT /api/v1/trips/{tripId}/flights/{flightId}
DELETE /api/v1/trips/{tripId}/flights/{flightId} -- Deleting means soft delete i.e. deletedAt is set 

## Hotels

POST /api/v1/trips/{tripId}/hotels
GET /api/v1/trips/{tripId}/hotels
PUT /api/v1/trips/{tripId}/hotels/{hotelId}
DELETE /api/v1/trips/{tripId}/hotels/{hotelId} -- Deleting means soft delete i.e. deletedAt is set 

## Notifications

GET /api/v1/notifications
PATCH /api/v1/notifications/{id}/read

---

# 10. Authentication

Implement Spring Security.

Use:

- Password hashing with BCrypt.
- JWT access tokens.
- Role-based authorization.

Public endpoints:

/api/v1/auth/**

Authenticated endpoints:

/api/v1/users/me
/api/v1/trips/**
/api/v1/notifications/**

Admin endpoints:

/api/v1/users/** where applicable.

Do not store plaintext passwords.

Do not put sensitive information into JWT claims.

JWT should contain only what is necessary, such as:

- subject/user ID
- roles
- issued-at
- expiration

Implement authentication cleanly enough that a future OAuth2/Keycloak integration could replace the current mechanism.

---

# 11. Search Abstraction

Even though Elasticsearch is NOT implemented in Phase 1, design trip searching with future Elasticsearch migration in mind.

Create a search abstraction, for example:

TripSearchService

with a method conceptually equivalent to:

search(TripSearchCriteria criteria, Pageable pageable)

Phase 1 implementation:

MySqlTripSearchService

Later:

ElasticsearchTripSearchService

Do not make controllers depend on the MySQL implementation.

TripSearchCriteria may contain:

- keyword
- destination
- status
- startDateFrom
- startDateTo
- ownerId

This is intentionally designed so Elasticsearch can later implement the same use case.

---

# 12. Event Abstraction

Do NOT add Kafka or RabbitMQ yet.

Create a small internal event abstraction.

Example:

DomainEventPublisher

Methods should allow publishing domain/application events.

Phase 1 implementation can simply:

- log the event, or
- persist audit information synchronously.

Do not introduce unnecessary asynchronous complexity.

Future implementations may include:

KafkaDomainEventPublisher
RabbitMqEventPublisher

Events to design for:

UserRegisteredEvent
TripCreatedEvent
TripUpdatedEvent
TripCancelledEvent
FlightAddedEvent
HotelAddedEvent
NotificationCreatedEvent

Do not prematurely implement distributed event handling.

---

# 13. Notification Design

For Phase 1:

TripService creates a Notification record synchronously.

Example:

Trip created
    ↓
TripService
    ↓
NotificationService
    ↓
Save notification to MySQL

Future architecture:

TripService
    ↓
EventPublisher
    ↓
Kafka/RabbitMQ
    ↓
NotificationConsumer
    ↓
NotificationService

Keep the NotificationService independent from transport technology.

---

# 14. Audit Design

Important business operations should generate audit records.

Avoid embedding audit-specific database code throughout controllers.

Prefer:

Business operation
    ↓
AuditService
    ↓
AuditLogRepository

Later:

Business operation
    ↓
Domain Event
    ↓
Kafka
    ↓
Audit Consumer
    ↓
AuditService

The Phase 1 implementation should therefore have a clear AuditService boundary.

---

# 15. Validation

Use Jakarta Bean Validation.

Examples:

User:

@NotBlank firstName
@NotBlank lastName
@Email email
@Size password

Trip:

@NotBlank name
@NotNull startDate
@NotNull endDate

Traveler:

@NotBlank firstName
@NotBlank lastName
@Email email

Flight:

@NotBlank airline
@NotBlank flightNumber
@NotBlank departureAirport
@NotBlank arrivalAirport
@NotNull departureTime
@NotNull arrivalTime

Use service-level validation for cross-field/business rules.

Do not attempt to solve all validation using annotations.

---

# 16. Pagination and Sorting

All collection endpoints should support pagination.

Use Spring Data Pageable.

Example:

GET /api/v1/trips?page=0&size=20&sort=startDate,desc

Implement sensible maximum page size.

Do not allow users to request unlimited records.

Return pagination metadata:

content
page
size
totalElements
totalPages
first
last

---

# 17. API Response Standards

Use consistent HTTP status codes.

Examples:

201 CREATED
200 OK
204 NO CONTENT
400 BAD REQUEST
401 UNAUTHORIZED
403 FORBIDDEN
404 NOT FOUND
409 CONFLICT
422 UNPROCESSABLE ENTITY if needed
500 INTERNAL SERVER ERROR

Use Location headers where appropriate for created resources.

Do not return internal exceptions to clients.

---

# 18. Logging

Use SLF4J.

Log:

- Application startup.
- Authentication failures.
- Important business operations.
- Unexpected exceptions.

Do NOT log:

- Passwords.
- JWT tokens.
- Sensitive personal information.
- Passport numbers.

Use structured logging where practical so it can later integrate with ELK/OpenSearch.

---

# 19. Configuration

Use environment variables for:

- Database URL
- Database username
- Database password
- JWT secret
- JWT expiration

Never commit secrets.

Create:

application.yml
application-dev.yml
application-test.yml

Use profiles.

Example:

dev
test
prod

Do not create a real production configuration containing credentials.

---

# 20. Docker

Phase 1 should include Docker Compose for MySQL only.

Example services:

mysql

Optionally Adminer for local development.

Do NOT add Elasticsearch/Kafka/RabbitMQ yet.

The Compose file should be easy to extend later.

---

# 22. Seed Data

Create development seed data only.

Include:

Admin user
2-3 normal users
Several trips
Travelers
Flights
Hotels
Activities
Notifications

Never use real personal information.

Document test credentials in README only if they are development-only credentials.

---

# 23. Swagger / OpenAPI

Add OpenAPI documentation.

Document:

- Authentication endpoints.
- Request models.
- Response models.
- Error responses.
- Pagination.
- Security requirements.

Swagger UI should be available during development.

---

# 24. README Requirements

README must explain:

1. Project purpose.
2. Architecture.
3. Technology stack.
4. Prerequisites.
5. How to start MySQL.
6. How to configure environment variables.
7. How to run the application.
8. How to run tests.
9. Swagger URL.
10. Database migration strategy.
11. Package structure.
12. Future architecture roadmap.

Include a section:

## Future Technology Roadmap

Phase 2:
Redis

Phase 3:
Elasticsearch

Phase 5:
Kafka

Phase 6:
Microservices

Phase 7:
Observability

Phase 8:
CI/CD + AWS

---

# 25. Architecture Documentation

Create:

docs/architecture/overview.md

Explain:

Client
  ↓
Controller
  ↓
Service
  ↓
Repository
  ↓
MySQL

Also document future extensions:

                    ┌── Redis
                    │
Spring Boot ────────┼── Elasticsearch
                    │
                    ├── RabbitMQ
                    │
                    └── Kafka

Create:

docs/architecture/decisions.md

Document important decisions:

- Why modular monolith?
- Why MySQL?
- Why Flyway?
- Why DTOs?
- Why service/repository separation?
- Why search abstraction?
- Why event abstraction?
- Why not microservices initially?

---

# 26. Coding Standards

Use:

- Java records where appropriate for immutable DTOs.
- Constructor injection.
- final fields.
- Meaningful names.
- Small focused methods.
- No field injection.
- No unnecessary abstractions.
- No generic "Utils" dumping ground.
- No business logic in controllers.
- No entity exposure through APIs.

Avoid:

@Autowired field injection

God classes

Huge service classes

Generic BaseService/BaseController abstractions unless justified

Premature microservices

Premature Kafka/RabbitMQ integration

---

# 27. Definition of Done

Phase 1 is complete only when:

- [ ] Project builds successfully with Maven.
- [ ] Application starts successfully.
- [ ] MySQL runs through Docker Compose.
- [ ] Flyway creates the database schema.
- [ ] Hibernate validates the schema.
- [ ] User registration works.
- [ ] Login works.
- [ ] JWT authentication works.
- [ ] Role-based authorization works.
- [ ] User profile works.
- [ ] Trip CRUD works.
- [ ] Trip ownership is enforced.
- [ ] Admin permissions work.
- [ ] Travelers CRUD works.
- [ ] Flights CRUD works.
- [ ] Hotels CRUD works.
- [ ] Activities CRUD works.
- [ ] Notifications work.
- [ ] Audit logging works.
- [ ] Pagination works.
- [ ] Filtering works.
- [ ] Validation works.
- [ ] Global exception handling works.
- [ ] OpenAPI documentation works.
- [ ] No secrets are committed.
- [ ] README is complete.
- [ ] Architecture documentation exists.
- [ ] Search abstraction exists.
- [ ] Event publishing abstraction exists.
- [ ] Code does not directly depend on Kafka/RabbitMQ/Elasticsearch.

---

# 28. Implementation Order

Claude Code should implement in this order.

## Step 1 — Project bootstrap

Create:

- Maven project.
- Spring Boot application.
- Dependencies.
- Package structure.
- application.yml.
- Profiles.
- README skeleton.

Run build.

## Step 2 — Docker + MySQL

Create Docker Compose.

Connect Spring Boot to MySQL.

Verify connection.

## Step 3 — Flyway

Create initial migration.

Verify database schema.

## Step 4 — Common infrastructure

Implement:

- Exception handling.
- API error model.
- Common pagination response.
- Auditing base fields if needed.
- Configuration properties.

## Step 5 — Users and roles

Implement:

- User entity.
- Role entity.
- Repositories.
- DTOs.
- Mapper.
- Service.
- Controller.
- Validation.

## Step 6 — Authentication

Implement:

- Spring Security.
- Password hashing.
- JWT generation.
- JWT validation.
- Authentication filter.
- Login.
- Registration.
- Role authorization.

Test thoroughly.

## Step 7 — Trips

Implement:

- Trip entity.
- Repository.
- DTOs.
- Mapper.
- Service.
- Controller.
- Filtering.
- Pagination.
- Ownership rules.
- Audit events.

## Step 8 — Travelers

Implement traveler CRUD under trips.

## Step 9 — Flights

Implement flight CRUD.

## Step 10 — Hotels

Implement hotel CRUD.

##

## Step 12 — Notifications

Implement notification persistence and retrieval.

## Step 13 — Audit

Implement AuditService and audit persistence.

## Step 14 — Search abstraction

Create TripSearchService and MySQL implementation.

Do not add Elasticsearch.

## Step 15 — Event abstraction

Create DomainEventPublisher.

Phase 1 implementation may log/persist events.

Do not add Kafka/RabbitMQ.

## Step 17 — OpenAPI

Complete API documentation.

## Step 18 — Documentation cleanup

Complete README and architecture documentation.

## Step 19 — Final quality pass

Check:

- Security.
- Validation.
- Transactions.
- N+1 queries.
- Lazy/eager relationships.
- Error handling.
- Logging.
- Secrets.
- Naming.
- API consistency.

---

# 29. Claude Code Instructions

You are implementing this project incrementally.

Do NOT attempt to generate the entire application in one step.

Before implementing each major module:

1. Inspect the existing project.
2. Explain the intended changes briefly.
3. Implement the module.
4. Run relevant tests.
5. Fix failures.
6. Review the implementation for architectural violations.
7. Update documentation.
8. Only then continue.

Never overwrite working code unnecessarily.

Do not introduce technologies that belong to later phases.

Do not add Kafka, RabbitMQ, Elasticsearch, Redis, Kubernetes, or microservices in Phase 1.

If a future technology requires an abstraction, implement the abstraction only when it has a clear purpose.

Prioritize clean architecture and maintainability over excessive abstraction.

Whenever there are multiple reasonable implementation choices, choose the simpler production-appropriate approach and document the decision.

---

# 30. Expected Final Architecture

At the end of Phase 1:

                    ┌──────────────────┐
                    │      Client      │
                    └────────┬─────────┘
                             │
                             ↓
                    ┌──────────────────┐
                    │  Spring Security │
                    │       + JWT      │
                    └────────┬─────────┘
                             │
                             ↓
                    ┌──────────────────┐
                    │   Controllers    │
                    └────────┬─────────┘
                             │
                             ↓
                    ┌──────────────────┐
                    │     Services     │
                    │                  │
                    │ User             │
                    │ Trip             │
                    │ Traveler         │
                    │ Flight           │
                    │ Hotel            │
                    │ Activity         │
                    │ Notification     │
                    │ Audit            │
                    └────────┬─────────┘
                             │
                ┌────────────┼────────────┐
                ↓            ↓            ↓
          Repositories   Search Port   Event Port
                │            │            │
                ↓            ↓            ↓
              MySQL      MySQL Search   Phase 1
                                       implementation

Future:

Search Port
    ↓
Elasticsearch

Event Port
    ↓
Kafka / RabbitMQ

Caching
    ↓
Redis

Deployment
    ↓
Docker → AWS

This architecture should make Phase 2+ additive rather than requiring a rewrite.
