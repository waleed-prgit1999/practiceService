# Architecture Decisions

## Why a modular monolith?

Phase 1 has one team, one deployable, and a small domain. Splitting into services now would add
network calls, distributed transactions, and deployment overhead without a scaling or
team-boundary problem that justifies it. Module boundaries (`auth`, `user`, `trip`,
`notification`, `audit`) are still enforced in code (package-private internals, controllers never
reach into another module's repository) so a later extraction to services is a matter of moving
packages, not redesigning them.

## Why MySQL?

Relational, transactional, well understood, and matches the domain: trips, travelers, flights and
hotel bookings are naturally relational with foreign keys and referential integrity requirements
(e.g. a traveler cannot outlive its trip). No requirement in Phase 1 calls for a document or
wide-column store.

## Why Flyway?

Schema changes need to be reviewable, ordered, and reproducible across dev/test/prod. Flyway
migrations are plain versioned SQL committed to the repo; Hibernate is configured with
`ddl-auto: validate` so it never silently mutates the schema — migrations are the single source of
truth for structure.

## Why DTOs (never expose entities)?

JPA entities carry persistence concerns (lazy proxies, bidirectional associations, cascade
behavior) that don't belong in an API contract, and exposing them directly couples the wire format
to the schema. Request/response DTOs let the schema evolve independently of the API and keep
serialization explicit (MapStruct-generated mappers).

## Why service/repository separation?

Controllers depend only on services; services own transactions and business rules; repositories
are Spring Data JPA interfaces with no business logic. This keeps ownership rules, cross-field
validation, and audit/notification side effects in one place (the service layer) instead of
scattered across controllers, and makes the business logic testable without a web layer or a
database.

## Why a search abstraction now?

`TripSearchService` (implemented by `MySqlTripSearchService` in Phase 1) is the seam where
Elasticsearch will be introduced in Phase 3. Controllers and other services depend on the
interface and `TripSearchCriteria`, never on the MySQL implementation, so adding
`ElasticsearchTripSearchService` later is additive.

## Why an event abstraction now?

`DomainEventPublisher` gives business code (e.g. `TripService`) one place to say "a trip was
created" without knowing whether that fact is currently just logged/persisted synchronously
(Phase 1) or will later be published to Kafka/RabbitMQ (Phase 5). This avoids a rewrite of service
code when messaging infrastructure is introduced — only the publisher implementation changes.

## Why not microservices initially?

No independent scaling needs, no independent team ownership, and no proven bounded-context
boundaries yet. Premature service extraction would mean guessing at boundaries and paying
distributed-systems costs (network failures, eventual consistency, service discovery) before
there's a concrete reason to. The modular monolith's package boundaries and ports (search, events)
are the deliberate preparation for that split later, from a codebase where the real seams have
already been observed.

## ID Strategy

**Decision: BIGINT auto-increment primary keys**, not UUIDs, for Phase 1.

Rationale: implementation simplicity was prioritized per the project brief. BIGINT identity
columns are simpler to model in JPA (`@GeneratedValue(strategy = GenerationType.IDENTITY)`),
smaller as index keys, and sufficient for a single-database modular monolith with no
multi-writer/offline-ID-generation requirement. IDs are still never exposed as raw database
implementation detail beyond "an opaque numeric identifier": every entity is mapped to a DTO
before crossing the API boundary, ownership/authorization checks happen in the service layer (not
by trusting a client-supplied ID's shape), and repositories are the only layer that queries by
primary key.

If a future phase needs non-guessable or globally-unique public identifiers (e.g. once trips are
referenced across services in Phase 6), a `UUID` column can be added to the affected tables and
exposed in DTOs without changing the internal BIGINT primary key — this is an additive change, not
a rewrite.

## Why no `activities` module in Phase 1

The initial brief referenced an `Activity` concept (in the recommended table list, seed data, and
definition-of-done) but never defined its fields or endpoints, unlike every other resource. Rather
than guess at a data model, it was deliberately scoped out of Phase 1: no `activities` table, no
`Activity` entity/controller/service, no seed data. Trips, travelers, flight segments and hotel
bookings cover the itinerary use case end-to-end without it. It can be added later the same way
flights and hotels were: a table, entity, DTOs, mapper, service and controller under `trip/`.

## Refresh tokens are stateless (no `refresh_tokens` table)

`POST /api/v1/auth/refresh` accepts a refresh JWT and issues a new access token. The refresh token
is a second JWT (longer-lived, marked with a `type: refresh` claim) signed with the same secret as
the access token — it is not persisted. This matches the recommended table list in the brief
(no `refresh_tokens` table) and keeps auth stateless, at the cost of not being able to revoke a
single refresh token before it expires. If per-token revocation becomes a requirement, a
`refresh_tokens` table (or a Redis-backed denylist, once Redis lands in Phase 2) can be introduced
without changing the access-token flow.
