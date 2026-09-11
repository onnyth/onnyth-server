# Architecture Decision Records

ADRs capture *why* a significant, hard-to-reverse decision was made — not implementation detail (that
belongs in `.agents/skills/`). Read these before proposing an alternative to an established pattern.

| ADR | Decision | Status |
|---|---|---|
| [ADR-0001](ADR-0001-hexagonal-architecture.md) | Per-feature hexagonal architecture | Accepted |
| [ADR-0002](ADR-0002-redis-idempotency.md) | Redis-backed idempotency keys for unsafe POST endpoints | Accepted |
| [ADR-0003](ADR-0003-kafka-domain-events.md) | Kafka for cross-feature domain events | Accepted |

## When to add a new ADR

- A new architectural style, or a pattern that a future agent might otherwise "helpfully" undo.
- A choice between two credible alternatives where the reasoning would otherwise be lost.
- A decision that supersedes a previous ADR — mark the old one `Superseded by ADR-000X` in its Status
  field, don't delete it.

Don't write an ADR for routine implementation details already covered by a skill's conventions.
