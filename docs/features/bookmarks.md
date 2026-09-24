# Bookmarks

## Status

Implemented, with one deployment-blocking gap (see Known Limitations).

## Purpose

Lets an authenticated user save, tag, and retrieve bookmarks (`url` + `title` + `tags`). This is the
**newest** feature module and exists primarily as the **reference implementation** for two
cross-cutting patterns not used anywhere else in the codebase: Redis-backed idempotent writes and
Kafka domain-event publishing.

## User Capabilities

- Create a bookmark (safely retryable via an idempotency key).
- Fetch a single bookmark by id.
- List bookmarks, paginated, optionally filtered by an exact tag match.
- Update a bookmark (full replace of url/title/tags).
- Delete a bookmark.

## Business Rules

- `POST` requires an `Idempotency-Key` header. Missing → 400. Same key + same canonical request body
  (`url|title|sortedTags`) → replays the cached response instead of creating a duplicate. Same key +
  different body → 409. See `docs/decisions/ADR-0002-redis-idempotency.md`.
- A `BookmarkCreated` event is published to Kafka **once per successful, non-replayed create**, after
  the DB transaction commits. See `docs/decisions/ADR-0003-kafka-domain-events.md`.
- `url` must be a valid URI (`@ValidUri`); `title` is required, max 255 chars; each tag max 50 chars.

## API

**Base**: `/api/v1/bookmarks` — authenticated (any valid Supabase JWT; **not** scoped to the caller,
see Known Limitations).

| Method | Path | Request | Response | Status |
|---|---|---|---|---|
| `POST` | `/` | `CreateBookmarkRequest` body + required `Idempotency-Key` header | `CreateBookmarkResponse` (with `Location` header) | 201 |
| `GET` | `/{id}` | — | `CreateBookmarkResponse` | 200 / 404 |
| `GET` | `/` | query: `page` (≥0, default 0), `size` (1–100, default 20), `tag` (optional exact match) | `BookmarkPageResponse` | 200 |
| `PUT` | `/{id}` | `BookmarkUpdateRequest` | `CreateBookmarkResponse` | 200 / 404 |
| `DELETE` | `/{id}` | — | — | 204 / 404 |

Feature-scoped error mapping via `BookmarkExceptionHandler`: `MissingIdempotencyKeyException` → 400,
`IdempotencyConflictException` → 409, `BookmarkNotFoundException` → 404.

## Data Model

`bookmark` table (`BookmarkEntity`): `id` (UUID PK), `url` (not null), `title` (not null, ≤255),
`created_at`/`updated_at` (set in `@PrePersist`/`@PreUpdate`). Tags live in a separate
`bookmark_tags` `@ElementCollection` table keyed by `bookmark_id`.

> [!WARNING]
> **No Flyway or Supabase migration creates the `bookmark`/`bookmark_tags` tables** — see Known
> Limitations. This is the single most important fact an agent must know before touching this feature.

## Domain Logic

```text
POST /api/v1/bookmarks (Idempotency-Key: K)
  → hash canonical "url|title|sortedTags"
  → Redis GET idempotency:K
      hit, same hash  → replay cached CreateBookmarkResponse (no new bookmark, no event)
      hit, diff hash  → 409 IdempotencyConflictException
      miss            → create Bookmark, publish BookmarkCreated after commit,
                         Redis SET idempotency:K {hash, response} (best-effort, 1-day TTL)
```
Redis read/write failures are caught and logged (`safeGetIdempotencyRecord` /
`safeSaveIdempotencyRecord`) — they never fail or roll back bookmark creation.

## Events

- **Publishes**: `bookmark.created.v1` (Kafka), payload `BookmarkCreated { eventId, eventType, occurredAt,
  bookmarkId, title, url }`, JSON-serialized, no producer type headers, keyed by `bookmarkId`.
  Reference consumer: `bookmark/adapter/in/kafka/BookmarkCreatedConsumer.java` (group `onnyth-bookmark`).
  Publish failures are caught and logged; they never fail the create request.

## Dependencies

- **Redis** (`shared.idempotency.adapter.out.RedisIdempotencyService`) — best-effort, fail-open.
- **Kafka** (`bookmark/adapter/out/kafka/*`) — best-effort, fail-open.
- **`shared.idempotency.application.IdempotencyService`** — the reusable idempotency port other
  features should adopt for the same guarantee (see `docs/decisions/ADR-0002-redis-idempotency.md`
  "Extending This Pattern").

## Key Files

| File | Role |
|---|---|
| `bookmark/domain/model/Bookmark.java` | Framework-free domain model |
| `bookmark/domain/event/BookmarkCreated.java` | Domain event record |
| `bookmark/application/usecase/BookmarkUseCaseService.java` | create/get/list-by-tag/update/delete; owns idempotency + publish orchestration |
| `bookmark/application/port/BookmarkRepository.java` | Outbound persistence port |
| `bookmark/application/port/out/BookmarkEventPublisher.java` | Outbound event port |
| `bookmark/adapter/in/rest/BookmarkController.java` | `/api/v1/bookmarks` REST controller |
| `bookmark/adapter/in/rest/BookmarkExceptionHandler.java` | Feature-scoped `@RestControllerAdvice` |
| `bookmark/adapter/in/kafka/BookmarkCreatedConsumer.java` | Reference `@KafkaListener` |
| `bookmark/adapter/out/persistence/{BookmarkEntity,BookmarkJpaRepository,BookmarkPersistenceMapper,BookmarkRepositoryAdapter}.java` | JPA persistence |
| `bookmark/adapter/out/kafka/{BookmarkKafkaTopics,KafkaBookmarkEventPublisher,KafkaTopicConfig}.java` | Topic constant, publisher, topic provisioning |
| `shared/idempotency/application/{IdempotencyService,IdempotencyResponse,IdempotencySerializer}.java` | Shared idempotency port, reusable by other features |
| `shared/idempotency/adapter/out/RedisIdempotencyService.java` | Redis adapter, key `idempotency:{key}`, 1-day TTL |

## Known Limitations

- **No schema migration.** `bookmark`/`bookmark_tags` have no `V*.sql` (Flyway) or `supabase/migrations/*`
  entry. Tests pass only because the test profile uses `ddl-auto=update`; any environment using
  `ddl-auto=validate` (i.e. production, per `application.properties`) will fail schema validation at
  startup. See `docs/development/known-issues.md` §1.
- **Not scoped to a user.** `Bookmark`/`BookmarkEntity` has no `userId`/owner field, and
  `BookmarkController` never reads the JWT subject. Any authenticated user can read, update, or delete
  any other user's bookmark. Confirm with the product spec ("UC-1", commit `f8f2e93`) whether this is
  intentionally global/shared before building more on top of it.
- Redis is not provisioned in `docker-compose.yml` — a contributor running only `docker compose up`
  will silently get "no idempotency" (fail-open, per ADR-0002) rather than an obvious error.

## Future Work

None currently scoped beyond fixing the two gaps above.
