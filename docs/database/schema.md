# Database Schema

Schema is owned entirely by Flyway migrations in `src/main/resources/db/migration`
(`V1__init_schema.sql`, `V2__seed_reference_roles.sql`, `V4__rename_columns_to_camel_case.sql`).
Hibernate runs with `ddl-auto: validate` — it checks entity mappings against this schema at
startup but never generates or alters it.

## Naming convention

**Columns are camelCase** (`firstName`, `createdAt`, `tripId`) and match the Java field names
exactly. Hibernate is configured with `PhysicalNamingStrategyStandardImpl` so field names pass
straight through, which is why entities carry almost no `@Column(name = ...)` declarations — the
field name *is* the column name.

**Tables stay snake_case** (`user_roles`, `flight_segments`, `audit_logs`), declared explicitly
via `@Table`. This is deliberate: MySQL folds table names to lowercase on Windows
(`lower_case_table_names=1` by default) but preserves case on Linux, so camelCase table names
would work in the Docker container and silently break against a native Windows MySQL. Column
names are not affected — they keep their stored case and compare case-insensitively.

V1–V3 created the schema in snake_case; `V4` renamed the columns. Those earlier migrations are
left as they were, since Flyway history records the schema as it actually existed at each point.

Dev-only sample data lives separately in `src/main/resources/db/seed` and is only applied when
the `dev` profile is active (see README, "Development Seed Data").

A consolidated, standalone version of the whole schema (all tables, indexes and the role
reference data in one runnable file) is kept at [full-schema.sql](full-schema.sql) for reference
and manual provisioning. Read the header before running it — hand-creating the schema requires
telling Flyway to baseline, or the app will fail on startup.

## Tables

| Table | Purpose | Notable columns |
|---|---|---|
| `users` | Accounts | `email` unique, `status` (`ACTIVE`/`DISABLED`) |
| `roles` | Reference data | `name` unique (`USER`, `ADMIN`) |
| `user_roles` | Many-to-many join | composite PK (`userId`, `roleId`) |
| `trips` | Trips owned by a user | `ownerId`, `status`, `startDateTime`/`endDateTime` (derived, see below), `deletedAt` (soft delete) |
| `travelers` | Travelers on a trip | `tripId`, `deletedAt` |
| `flight_segments` | Flights on a trip | `tripId`, `status`, `deletedAt` |
| `hotel_bookings` | Hotel stays on a trip | `tripId`, `status`, `deletedAt` |
| `notifications` | Per-user notifications | `userId`, `isRead` (not `read` — reserved word in MySQL) |
| `audit_logs` | Audit trail | `userId` (nullable, `ON DELETE SET NULL`), `entityType`/`entityId`, `metadata` (JSON) |

There is deliberately no `activities` table in Phase 1 — see decisions.md, "Why no `activities`
module in Phase 1".

## `trips.startDateTime` / `endDateTime`

These columns are **not** set directly by clients. `TripService` recomputes them from the trip's
non-deleted flight segments and hotel bookings every time one is added, updated or removed:
`startDateTime` = earliest departure/check-in, `endDateTime` = latest arrival/check-out. A
trip with no flights or hotels yet has both columns `NULL`.

## Soft deletes

`trips`, `travelers`, `flight_segments` and `hotel_bookings` use a `deletedAt` timestamp instead
of a hard `DELETE`. Cancelling a trip (`DELETE /api/v1/trips/{id}`) sets `deletedAt` and
`status = CANCELLED` on the trip and cascades `deletedAt` to all of its travelers, flights and
hotel bookings in the same transaction. All read queries filter on `deletedAt IS NULL`.

## Indexes

| Index | Reason |
|---|---|
| `users(email)` (unique) | login lookup |
| `users(status)` | admin filtering |
| `trips(ownerId)` | "my trips" / ownership checks |
| `trips(status)`, `trips(startDateTime)`, `trips(destination)` | search/filter endpoints |
| `travelers/flight_segments/hotel_bookings(tripId)` | itinerary assembly |
| `notifications(userId, isRead)` | notification list/unread queries |
| `audit_logs(userId)`, `audit_logs(entityType, entityId)`, `audit_logs(createdAt)` | audit retrieval |

## ID strategy

BIGINT auto-increment primary keys everywhere — see decisions.md, "ID Strategy", for the
reasoning and the migration path to opaque public IDs if a later phase needs them.
