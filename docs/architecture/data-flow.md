# Data Flow

## Write-path data flow (the common case)

```text
Client → Controller (validate shape) → Use case (validate business rules, orchestrate)
       → Port → Persistence adapter → PostgreSQL (single transaction, ddl-auto=validate in prod)
```

Nearly every feature's writes are simple, synchronous, single-transaction read-modify-write operations
against PostgreSQL. There is no CQRS split, no outbox table, and no eventual-consistency requirement
for the vast majority of the system — see `docs/data/data-consistency.md` for the two features that
are exceptions.

## Cross-feature read composition

Several read endpoints assemble data owned by multiple features into a single response DTO, entirely
within one request/transaction — there is no caching or pre-aggregation layer:

- **Profile card** (`GET /api/v1/profile/card`): `user` (identity/score/rank/level/cosmetics) +
  `streak` (current streak) + `lifestats` (5 domain scores) + `leveling` (level title) assembled live
  by `ProfileUseCaseService`. See `docs/features/profiles.md`.
- **Feed** (`GET` friend feed): `feed` events sourced from writes in `activity`, `leveling`,
  `achievement`, and `streak` — see `docs/features/feed.md` for exactly which event types are included.
- **Leaderboard** (`GET /api/v1/leaderboard`): `leaderboard` reads `friendship` (friend graph) +
  `user` (scores) — see `docs/features/leaderboard.md`.

These compositions happen synchronously, in-process, at request time — not via a background job or
materialized view. There is no cache in front of any of these reads today.

## Event-driven internal data flow

The only in-process event flow is `shared.domain.event.StatChangedEvent`: a write to a `lifestats`
domain table (via `registration`'s commit flow, or any direct `lifestats` update) publishes this
event; `scoring`'s `@EventListener` recomputes `User.totalScore`; `scoring` then triggers `ranking` to
recompute `User.rankTier` if it changed. This is a synchronous, same-thread Spring
`ApplicationEvent` — not asynchronous, not a message queue.

## Asynchronous / decoupled data flow

The only asynchronous, broker-mediated data flow in the system today is `bookmark`'s Kafka event:
`BookmarkCreated` is published to `bookmark.created.v1` after the creating transaction commits, and
consumed independently by any listener in its own consumer group. No other feature publishes or
consumes Kafka events. See `docs/decisions/ADR-0003-kafka-domain-events.md`.

## External data flow

```text
auth/     → outbound HTTP → Supabase Auth (auth/v1/{signup,token,logout})
profile/  → outbound HTTP → Supabase Storage (storage/v1/object/{bucket}/{path})
(all)     → JDBC          → PostgreSQL (Supabase-hosted, ddl-auto=validate)
```

No feature calls any other third-party API. There is no outbound webhook, email, or push-notification
integration today (see `docs/development/future-work.md` for planned notification work).

## Idempotency and caching

Redis is used for exactly one purpose today: `bookmark`'s idempotency-key cache
(`idempotency:{key}` → `{requestHash, serializedResponse}`, 1-day TTL, best-effort/fail-open). It is
not used as a general application cache, session store, or rate limiter anywhere else in the codebase.
