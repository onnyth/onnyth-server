# Data Consistency

## The default: single-transaction, strongly consistent

The overwhelming majority of writes in this system are a single `@Transactional` use-case method
performing one or more JPA operations against PostgreSQL, committed or rolled back atomically. There is
no distributed transaction, no saga, and no CQRS read/write split anywhere in the codebase. If a use
case throws, the transaction rolls back and the client receives an error — no partial state is ever
visible.

## Exception 1: in-process event-driven recalculation (synchronous, same transaction boundary)

`shared.domain.event.StatChangedEvent` is a plain Spring `ApplicationEvent`, published synchronously
(Spring's default `ApplicationEventMulticaster` is synchronous unless configured otherwise — nothing in
this codebase configures it to be asynchronous). The listener in `scoring/` runs in the same thread,
and its own `@Transactional` method commits independently of the publisher's transaction. This means:
- If the *publishing* write's transaction later fails for an unrelated reason after the event has
  already been handled, the score recalculation is **not** automatically rolled back with it (they are
  separate transactions). In practice this is a narrow window since the publish happens near the end of
  a use-case method — but it is not one atomic unit across both.
- There is no retry, no dead-letter path, and no idempotency check for this event — verify actual
  behavior in `docs/features/scoring.md` before assuming stronger guarantees.

## Exception 2: Kafka + Redis (bookmark only) — deliberately best-effort, fail-open

This is the **one place in the system with an accepted eventual-consistency / at-most-once tradeoff**,
and it is fully intentional (see `docs/decisions/ADR-0002-redis-idempotency.md` and
`docs/decisions/ADR-0003-kafka-domain-events.md`):

- **Idempotency cache (Redis)**: best-effort. A Redis outage does not fail bookmark creation — it just
  means a client retry during that outage could create a duplicate bookmark. Accepted tradeoff.
- **Domain event (Kafka)**: best-effort, at-most-once, deferred to after-commit. A Kafka outage means
  the `BookmarkCreated` event is silently lost — no retry queue, no outbox table. The bookmark write
  itself always succeeds regardless. If a future consumer of this event becomes business-critical
  (not just advisory), this tradeoff should be revisited via a new ADR.

No other feature uses Redis or Kafka, so no other feature has this class of consistency tradeoff today.

## Schema consistency: the migration-history gap

The most significant **structural** consistency risk in the system today is not a runtime data-race —
it's that the two schema-migration histories (Flyway vs. Supabase CLI) are not equivalent, and roughly
half the tables in the system have no Flyway migration at all. See `docs/data/overview.md` and
`docs/development/known-issues.md` for the full detail. Practically: do not assume a fresh
Flyway-only bootstrap of the database would produce a working schema — it would not, today.

## Idempotency as a pattern (not yet reused)

The `bookmark` feature's `shared.idempotency.application.IdempotencyService` port is designed to be
reused by any future unsafe (`POST`) endpoint that needs the same retry-safety guarantee. No other
feature has adopted it yet — every other `POST` endpoint in the system is **not** idempotent (a client
retry after a timeout could create a duplicate resource, e.g. a duplicate quest completion attempt
would be rejected by a unique constraint, but a duplicate friend request send is not guarded the same
way — verify per-feature in `docs/features/*.md`).
