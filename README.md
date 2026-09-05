# Travel Management Platform

Phase 1 backend for a Travel Management Platform, built as a Spring Boot modular monolith.

> Status: under active development. This README is updated as each module lands.

## 1. Project Purpose

A backend service that lets registered users manage their travel: create trips, attach travelers,
flight segments and hotel bookings, view a consolidated itinerary, and receive notifications.
Administrators can manage users and inspect system-wide activity via an audit trail.

Phase 1 deliberately excludes real airline/hotel integrations, payments, caching, search engines,
and messaging infrastructure — see [Future Technology Roadmap](#future-technology-roadmap).

## 2. Architecture

Modular monolith: a single deployable Spring Boot application internally organized into
feature modules (`auth`, `user`, `trip`, `notification`, `audit`, `common`), each with its own
controller/service/repository/entity/dto/mapper layers. See
[docs/architecture/overview.md](docs/architecture/overview.md) for the request flow and
[docs/architecture/decisions.md](docs/architecture/decisions.md) for the reasoning behind key
choices.

## 3. Technology Stack

- Java 21, Spring Boot 3.5
- Spring Web, Spring Data JPA, Hibernate
- MySQL 8, Flyway
- Spring Security, JWT (jjwt)
- Bean Validation, MapStruct, Lombok
- springdoc-openapi (Swagger UI)
- JUnit 5, Mockito, Testcontainers

## 4. Prerequisites

- JDK 21+
- Maven (or use the bundled `./mvnw`)
- MySQL 8.0+ — either a local install, or Docker + Docker Compose

## 5. Starting MySQL

Pick whichever you have. Either way the app connects to `localhost:3306` as `travel_user`
against the `travel_platform` database, and Flyway creates the tables on first startup.

### Option A — locally installed MySQL

Create the database and application user once, as root. Run from the project root.

macOS / Linux / Git Bash:

```bash
mysql -u root -p < docs/database/bootstrap-local-mysql.sql
```

Windows PowerShell — note that PowerShell does **not** support `<` redirection, so pipe the
file in instead, and use the client's full path since it is normally not on `PATH`:

```powershell
Get-Content docs\database\bootstrap-local-mysql.sql |
    & "C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" -u root -p
```

Or run it from a GUI: open the file in MySQL Workbench, connect as root, and execute it.

See [docs/database/bootstrap-local-mysql.sql](docs/database/bootstrap-local-mysql.sql) — it
creates only the empty database and the `travel_user` account, matching the defaults in
`application-dev.properties`. Skipping this step is what produces
`Access denied for user 'travel_user'@'localhost'` at startup.

### Option B — Docker

```bash
docker compose up -d
```

This starts a MySQL 8 instance and Adminer (at http://localhost:8081) for local inspection,
provisioning the database and user automatically. No bootstrap script needed.

> Note: the Testcontainers-based integration tests (`./mvnw test`) require Docker regardless
> of which option you use for running the app, since they start their own disposable MySQL.

## 6. Environment Variables

| Variable | Purpose | Local default |
|---|---|---|
| `DB_HOST` | MySQL host | `localhost` |
| `DB_PORT` | MySQL port | `3306` |
| `DB_NAME` | MySQL database name | `travel_platform` |
| `DB_USERNAME` | MySQL user | `travel_user` |
| `DB_PASSWORD` | MySQL password | `travel_pass` |
| `JWT_SECRET` | HMAC signing secret for JWTs | dev-only fallback in `application-dev.properties` |
| `JWT_ACCESS_TOKEN_EXPIRATION_MS` | Access token lifetime | `900000` (15 min) |
| `JWT_REFRESH_TOKEN_EXPIRATION_MS` | Refresh token lifetime | `604800000` (7 days) |
| `DEFAULT_NEW_USER_PASSWORD` | Password given to admin-created accounts | `Pakistan123` |

The `dev` profile ships with insecure local-only defaults so the app runs out of the box;
never reuse them outside local development. No production configuration file is committed —
production deployments must supply all variables above via the environment.

## 7. Running the Application

```bash
./mvnw spring-boot:run
```

Runs with the `dev` profile by default (see `spring.profiles.active` in `application.properties`).

## 8. Running Tests

```bash
./mvnw test
```

Integration tests use Testcontainers to spin up a disposable MySQL instance — no local
MySQL setup is required to run the test suite, only a working Docker daemon.

## 9. Swagger / OpenAPI

Once the app is running:

- Swagger UI: http://localhost:8080/swagger-ui.html
- OpenAPI JSON: http://localhost:8080/v3/api-docs

## 10. Database Migration Strategy

Schema is managed exclusively through Flyway migrations in `src/main/resources/db/migration`,
versioned as `V<N>__description.sql`. Hibernate runs with `ddl-auto: validate` — it never
generates or alters schema, it only checks the entity mappings match what Flyway created.

## 11. Package Structure

```
com.example.travel
├── TravelApplication.java
├── common/        # cross-cutting: error handling, pagination, base config
├── auth/          # authentication, JWT, security config
├── user/          # user profile, roles, admin user management
├── trip/          # trips, travelers, flight segments, hotel bookings, search
├── notification/  # notification persistence and retrieval
└── audit/         # audit logging
```

Each feature module follows `controller/ -> service/ -> repository/ -> entity/`, with
`dto/`, `mapper/`, `exception/` and `validation/` alongside as needed. Controllers never call
repositories directly, and entities are never returned from REST endpoints.

## 12. Future Technology Roadmap

Phase 1 leaves clean extension points (a search port and an event-publishing port) so the
following can be introduced additively, without rewriting business logic:

- **Phase 2:** Redis (caching)
- **Phase 3:** Elasticsearch (trip search)
- **Phase 5:** Kafka (event streaming)
- **Phase 6:** Microservices
- **Phase 7:** Observability
- **Phase 8:** CI/CD + AWS

## Admin-created users

`POST /api/v1/users` (ADMIN only) creates an account without the admin choosing a password. The
new user is always `ACTIVE` with the `USER` role — this endpoint cannot create administrators —
and gets the password from `DEFAULT_NEW_USER_PASSWORD` (`Pakistan123` by default).

```bash
curl -X POST http://localhost:8080/api/v1/users \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $ADMIN_ACCESS_TOKEN" \
  -d '{"firstName":"Nadia","lastName":"Iqbal","email":"nadia@example.com"}'
```

Returns `201` with a `Location` header. The password is never included in any response, so
whoever creates the account has to pass it on separately.

> **A shared default password is a real weakness.** Every account created this way has the same
> known password until someone changes it, and Phase 1 has no password-reset or force-change-on-
> first-login flow, so nothing compels a change. It is acceptable for local development only.
> Before this goes anywhere real, either set `DEFAULT_NEW_USER_PASSWORD` to a per-environment
> secret, or replace the fixed default with a generated one-time password plus a reset flow.

## Development Seed Data

The `dev` profile additionally applies migrations from `src/main/resources/db/seed`
(`spring.flyway.locations` in `application-dev.properties`), which seed:

- An admin user, three regular users (one `DISABLED`, to exercise that path)
- Three trips, with flight segments, hotel bookings and travelers on two of them
- A few notifications

| Email | Role | Status | Password |
|---|---|---|---|
| admin@travel.local | ADMIN | ACTIVE | `DevPass123!` |
| alice@travel.local | USER | ACTIVE | `DevPass123!` |
| bilal@travel.local | USER | ACTIVE | `DevPass123!` |
| carla@travel.local | USER | DISABLED | `DevPass123!` |

These are development-only credentials seeded solely by the `dev` profile — they are never valid
outside a local environment and no real personal information is used.
