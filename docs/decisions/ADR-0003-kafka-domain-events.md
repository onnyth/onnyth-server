# ADR-0003: Kafka for Cross-Feature Domain Events

## Status

Accepted.

## Context

The `bookmark` feature needed to notify other (potential future) consumers when a bookmark is created,
without the creating request depending on those consumers being available or fast, and without a
publish failure ever rolling back or failing the write. See commits `c502a6d`, `ec05c58`, `fbd896d`.

## Decision

- A single-node Kafka broker (KRaft mode, no Zookeeper) runs via `docker-compose.yml` for local dev.
- Each publishing feature defines its own topic name(s) as a constant, versioned in the name, e.g.
  `bookmark.created.v1` (`bookmark/adapter/out/kafka/BookmarkKafkaTopics.java`).
- Events are plain domain records (e.g. `bookmark/domain/event/BookmarkCreated.java`: `eventId`,
  `eventType`, `occurredAt`, plus the event's own data fields), serialized as JSON
  (`spring.kafka.producer.value-serializer=...JsonSerializer`), **without** leaking the producer's
  Java class name to consumers (`spring.kafka.producer.properties.spring.json.add.type.headers=false`)
  — the topic's published contract (the event record) is what consumers rely on, not a Java type.
- Events are keyed by the aggregate id (e.g. `bookmarkId`) for partition ordering per-entity.
- `spring.kafka.producer.properties.max.block.ms=2000` — a broker/metadata outage cannot block the
  calling HTTP thread for Kafka's 60s default.
- Publishing is **best-effort and asynchronous relative to the HTTP response**: the use case catches
  and logs any publish failure; the write (e.g. bookmark creation) always succeeds regardless of Kafka
  availability. This is the same fail-open precedent applied to Redis idempotency (ADR-0002).
- When a use case both writes to the DB and publishes an event, the publish is deferred until
  **after the transaction commits** via
  `TransactionSynchronizationManager.registerSynchronization(...).afterCommit()`, falling back to an
  immediate publish if no transaction is active. This avoids publishing an event for a write that is
  later rolled back.
- An event is published **exactly once per successful, non-replayed write** — an idempotent-replay
  response (same idempotency key + same body) does not re-publish.
- Consumers use their own dedicated `groupId` (e.g. `onnyth-bookmark` in
  `bookmark/adapter/in/kafka/BookmarkCreatedConsumer.java`, distinct from the general
  `spring.kafka.consumer.group-id=onnyth`), and declare the exact event type they expect via
  `spring.kafka.consumer.properties.spring.json.value.default.type`.

## Alternatives Considered

Not documented beyond the above. No evidence of an outbox-table or other transactional-messaging
pattern being evaluated; the current implementation is a direct producer call deferred to
after-commit, not a transactional outbox.

## Why This Decision

Directly evidenced in commit messages: decouple event consumers from the producer's internal
persistence/Java types (JSON contract, no type headers); make publishing failure-tolerant so a
messaging outage never blocks or fails a user-facing write (`max.block.ms`, try/catch + log); avoid
publishing events for writes that get rolled back (after-commit synchronization).

## Consequences

- **Easier**: adding new consumers for `bookmark.created.v1` without touching the producer; safe to
  retry the HTTP request without duplicate side effects downstream, since the publish only happens
  once per genuine write.
- **Harder / accepted risk**: because publish failures are swallowed, a Kafka outage causes **silent
  event loss** — there is no retry queue or dead-letter topic. If a downstream consumer becomes
  business-critical (not just advisory), this fail-open behavior should be revisited (a new ADR should
  supersede this one before changing it).
- Kafka is currently provisioned and used only for this one feature — do not assume other features
  have an event bus without checking; see `AGENTS.md`.

## Implementation

- `docker-compose.yml` (`kafka` service, KRaft single-node)
- `src/main/resources/application.properties` (`spring.kafka.*`)
- `bookmark/domain/event/BookmarkCreated.java`
- `bookmark/adapter/out/kafka/{BookmarkKafkaTopics,KafkaBookmarkEventPublisher,KafkaTopicConfig}.java`
- `bookmark/adapter/in/kafka/BookmarkCreatedConsumer.java`
- `bookmark/application/port/out/BookmarkEventPublisher.java` (port implemented by the Kafka adapter)

## Related

- `docs/decisions/ADR-0001-hexagonal-architecture.md` (publisher as an outbound port/adapter)
- `docs/decisions/ADR-0002-redis-idempotency.md` (matching best-effort/fail-open precedent)
