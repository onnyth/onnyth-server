# ADR-0002: Redis-backed Idempotency Keys for Unsafe POST Endpoints

## Status

Accepted.

## Context

`POST /api/v1/bookmarks` must be safely retryable by clients (per an internal use-case spec referred to
in commit messages as "UC-1"): a client retry after a timeout or dropped response must not create a
duplicate bookmark, and a retry with a *different* body under the same key must be rejected rather than
silently accepted. See `bookmark/application/usecase/BookmarkUseCaseService.java` and commit `f8f2e93`.

## Decision

- Clients must send an `Idempotency-Key` header on `POST /api/v1/bookmarks`; a missing key returns
  `400` via `MissingIdempotencyKeyException` (`bookmark/adapter/in/rest/BookmarkController.java`).
- The use case hashes a **canonical** representation of the request (`url|title|sortedTags`, not
  `toString()`, so `Set` iteration order can't cause a false conflict) and stores
  `{requestHash, serializedResponse}` in Redis under `idempotency:{key}` for 1 day
  (`shared/idempotency/adapter/out/RedisIdempotencyService.java`).
- Same key + same hash → replay the cached response instead of creating a new bookmark.
- Same key + different hash → `409 Conflict` via `IdempotencyConflictException`.
- The idempotency cache is treated as **best-effort**: a Redis read/write failure is caught and logged,
  not propagated — a bookmark is still created (and the request does not fail) even if Redis is down.
  This is a deliberate, documented tradeoff: a retry during a Redis outage may create a duplicate
  bookmark, which is accepted as preferable to failing bookmark creation because of a cache outage.
- The idempotency contracts (`IdempotencyService`, `IdempotencyResponse`, `IdempotencySerializer`) live
  in `shared.idempotency.application`; Redis is an adapter (`shared.idempotency.adapter.out`) behind
  that port, not hardwired into the use case.

## Alternatives Considered

Not documented beyond the above. No evidence of a DB-backed idempotency table or in-memory alternative
being evaluated in the repository.

## Why This Decision

Directly evidenced in commit `f8f2e93`: Redis gives fast, TTL'd storage suited to a short-lived
dedup window; the shared `IdempotencyService` port keeps the storage technology swappable per the
hexagonal architecture (ADR-0001), and the best-effort/fail-open behavior mirrors the same tradeoff
accepted for Kafka publish failures (see ADR-0003).

## Consequences

- **Easier**: safe client retries for bookmark creation; the pattern (port + Redis adapter +
  best-effort wrapper methods) is copy-paste reusable for any future unsafe endpoint that needs the
  same guarantee.
- **Harder**: Redis is now a required runtime dependency for this one endpoint to be fully idempotent,
  but `docker-compose.yml` does not provision a Redis service (see `docs/known-issues.md`) — a
  contributor running only `docker compose up` will not have Redis available.
- A duplicate bookmark is possible during a Redis outage combined with a client retry — accepted, not
  a bug.

## Implementation

- `shared/idempotency/application/{IdempotencyService,IdempotencyResponse,IdempotencySerializer}.java`
- `shared/idempotency/adapter/out/RedisIdempotencyService.java`
- `bookmark/application/usecase/BookmarkUseCaseService.java` (`safeGetIdempotencyRecord` /
  `safeSaveIdempotencyRecord`)
- `bookmark/application/exception/{MissingIdempotencyKeyException,IdempotencyConflictException}.java`

## Related

- `docs/decisions/ADR-0001-hexagonal-architecture.md` (port/adapter shape)
- `docs/decisions/ADR-0003-kafka-domain-events.md` (same best-effort/fail-open precedent)
- `docs/known-issues.md` (Redis not in `docker-compose.yml`)
