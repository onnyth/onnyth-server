# Active Work

There is **no work currently in progress**. The most recently shipped work (per git history) was the
`bookmark` feature (Redis idempotency + Kafka domain events) — see
`docs/decisions/ADR-0002-redis-idempotency.md` and `docs/decisions/ADR-0003-kafka-domain-events.md`.
Before that, the codebase completed a full migration to the per-feature hexagonal architecture (see
`docs/decisions/ADR-0001-hexagonal-architecture.md`) and shipped activity/leveling/streak/feed/store
functionality (see `docs/features/`).

## How to start new active work

There is no separate planning/ticketing system in this repository — plan directly against `docs/`:

1. Read `docs/development/known-issues.md` and `docs/development/future-work.md` for already-scoped
   next steps, or define a new one.
2. Read the relevant `docs/features/<feature>.md` and `docs/architecture/dependency-rules.md`.
3. Implement following `docs/ai/development-rules.md`.
4. Update this file while work is in progress (see format below), and update the relevant
   `docs/features/*.md` / `docs/development/current-state.md` / `docs/development/known-issues.md`
   when it's done — see `docs/ai/documentation-rules.md`.

## What this file is for

While work is actively in progress, update this file to reflect it:
```markdown
## <Work Item Name>

Status: In Progress

Current work:
- ...

Remaining:
- ...
```
Clear it back to "no work in progress" once the change is complete and the rest of `docs/` has been
updated to reflect it.
