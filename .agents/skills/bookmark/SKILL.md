---
name: bookmark
description: Bookmark CRUD with Redis-backed idempotent creation and Kafka BookmarkCreated event publishing — the reference pattern for idempotency + domain events
---

# Bookmarks

User bookmark CRUD (`url`, `title`, `tags`). This is the newest feature module and is the **reference
implementation** for two cross-cutting patterns not yet used elsewhere: idempotent unsafe writes
(Redis) and domain event publishing (Kafka). See `docs/decisions/ADR-0002-redis-idempotency.md` and
`docs/decisions/ADR-0003-kafka-domain-events.md` for the full rationale — this skill covers the
feature's shape and behavior.

## Key Files

| File | Role |
|---|---|
| `bookmark/domain/model/Bookmark.java` | Framework-free domain model (`id`, `url`, `title`, `tags`, `createdAt`, `updatedAt`) |
| `bookmark/domain/event/BookmarkCreated.java` | Domain event record, published once per successful (non-replayed) create |
| `bookmark/application/usecase/BookmarkUseCaseService.java` | create/get/list-by-tag/update/delete; owns idempotency + event-publish orchestration |
| `bookmark/application/port/BookmarkRepository.java` | Outbound persistence port |
| `bookmark/application/port/out/BookmarkEventPublisher.java` | Outbound event-publishing port |
| `bookmark/application/exception/{BookmarkNotFoundException,MissingIdempotencyKeyException,IdempotencyConflictException}.java` | 404 / 400 / 409 respectively |
| `bookmark/adapter/in/rest/BookmarkController.java` | `/api/v1/bookmarks` REST controller |
| `bookmark/adapter/in/rest/BookmarkExceptionHandler.java` | Feature-scoped `@RestControllerAdvice(assignableTypes = BookmarkController.class)` — maps the three exceptions above to `ApiErrorResponse` |
| `bookmark/adapter/in/kafka/BookmarkCreatedConsumer.java` | Reference `@KafkaListener` for `bookmark.created.v1` (group `onnyth-bookmark`) |
| `bookmark/adapter/out/persistence/{BookmarkEntity,BookmarkJpaRepository,BookmarkPersistenceMapper,BookmarkRepositoryAdapter}.java` | JPA persistence, table `bookmark` (+ `@ElementCollection` table `bookmark_tags`) |
| `bookmark/adapter/out/kafka/{BookmarkKafkaTopics,KafkaBookmarkEventPublisher,KafkaTopicConfig}.java` | Topic constant `bookmark.created.v1`, publisher, topic provisioning |
| `shared/idempotency/application/{IdempotencyService,IdempotencyResponse,IdempotencySerializer}.java` | Shared idempotency port + record + (de)serializer, reusable by other features |
| `shared/idempotency/adapter/out/RedisIdempotencyService.java` | Redis adapter, key `idempotency:{key}`, 1-day TTL |

## API

**Base**: `/api/v1/bookmarks` — **authenticated** (falls under `SecurityConfig`'s
`anyRequest().authenticated()`; any valid Supabase JWT works). Note: bookmarks are **not** scoped to
the requesting user — `Bookmark` has no `userId`/owner field and the controller never reads
`@AuthenticationPrincipal Jwt`, so any authenticated user can read/update/delete any bookmark.

| Method | Path | Request | Response | Status | Notes |
|---|---|---|---|---|---|
| `POST` | `/` | `CreateBookmarkRequest` body + **required** `Idempotency-Key` header | `CreateBookmarkResponse` | 201 | `Location` header set to `/{id}` |
| `GET` | `/{id}` | — | `CreateBookmarkResponse` | 200 / 404 | |
| `GET` | `/` | query: `page` (≥0, default 0), `size` (1-100, default 20), `tag` (optional exact match) | `BookmarkPageResponse` | 200 | no `tag` → all bookmarks paginated |
| `PUT` | `/{id}` | `BookmarkUpdateRequest` | `CreateBookmarkResponse` | 200 / 404 | full replace of url/title/tags |
| `DELETE` | `/{id}` | — | — | 204 / 404 | |

### Error responses (via `BookmarkExceptionHandler`)
- `400` — missing `Idempotency-Key` header (`MissingIdempotencyKeyException`)
- `409` — `Idempotency-Key` reused with a different request body (`IdempotencyConflictException`)
- `404` — bookmark not found (`BookmarkNotFoundException`), from get/update/delete
- Validation errors (`@Valid` on request DTOs) fall through to the shared
  `GlobalExceptionHandler` (see `error-handling/SKILL.md`)

### DTOs
```java
CreateBookmarkRequest(String url, String title, Set<String> tags)   // url: @ValidUri; title: max 255
BookmarkUpdateRequest(String url, String title, Set<String> tags)   // same validation
CreateBookmarkResponse(UUID id, String url, String title, Set<String> tags, Instant createdAt, Instant updatedAt)
BookmarkPageResponse                                                 // paginated wrapper, see BookmarkPageResponse.fromPage(Page<Bookmark>)
```

## Idempotent Create Flow

```text
POST /api/v1/bookmarks (Idempotency-Key: K)
  → hash canonical "url|title|sortedTags"
  → Redis GET idempotency:K
      hit, same hash  → replay cached CreateBookmarkResponse (no new bookmark, no event)
      hit, diff hash  → 409 IdempotencyConflictException
      miss            → create Bookmark, publish BookmarkCreated after commit,
                         Redis SET idempotency:K {hash, response} (best-effort, TTL 1 day)
```
Redis read/write failures are caught and logged (`safeGetIdempotencyRecord`/`safeSaveIdempotencyRecord`)
— they never fail or roll back bookmark creation. See ADR-0002 for the accepted tradeoff.

## Event: `BookmarkCreated`

- Topic `bookmark.created.v1`, keyed by `bookmarkId`, JSON payload, no Java type headers.
- Published **once per successful, non-replayed create**, deferred until after the DB transaction
  commits (`TransactionSynchronizationManager`).
- Publish failures are caught and logged; they never fail the create request. See ADR-0003.
- Fields: `eventId`, `eventType` (`"BookmarkCreated"`), `occurredAt`, `bookmarkId`, `title`, `url`.

## Data Model

`bookmark` table (`BookmarkEntity`): `id` (UUID PK), `url` (not null, validated URI), `title` (not
null, ≤255), `created_at`/`updated_at` (not null, set in `@PrePersist`/`@PreUpdate`); tags in a
separate `bookmark_tags` element-collection table keyed by `bookmark_id`.

> [!WARNING]
> **No Flyway or Supabase migration creates the `bookmark`/`bookmark_tags` tables.** Tests pass only
> because the test profile uses `ddl-auto=update`; production uses `ddl-auto=validate` and will fail
> schema validation without a migration. See `docs/known-issues.md` §1 before deploying this feature.

## Tests

`src/test/java/com/onnyth/onnythserver/bookmark/`: `BookmarkControllerTest` (idempotency header
handling), `BookmarkUseCaseServiceTest` (cache-miss create, matching-retry replay, conflict on
different body, tag-order hash stability, Redis read/write failure paths, publish-on-create vs.
no-publish-on-replay), `BookmarkEntityValidationTest`, `CreateBookmarkRequestValidationTest`.

## Extending This Pattern

If a new feature needs the same guarantees:
1. Reuse `shared.idempotency.application.IdempotencyService` — don't build a new idempotency
   mechanism; inject the existing port.
2. Reuse the "canonical string hash + safe get/save wrapper" shape from
   `BookmarkUseCaseService` rather than re-deriving it.
3. For events, define your own `{feature}.{event}.v1` topic constant and a domain event record in your
   own `domain/event` package — don't publish to `bookmark.created.v1` or reuse `BookmarkCreated`.
4. Keep publish best-effort (catch + log) unless the new use case has a documented requirement for
   at-least-once delivery — that would need its own ADR superseding/extending ADR-0003.
