# Architecture Rules for AI Agents

Architectural boundaries an AI agent must treat as deliberate and must not "helpfully" undo, simplify,
or route around — even if a shortcut would technically work.

## Rules that exist because they were decided, not because no one got to fixing them

1. **Per-feature hexagonal structure is permanent**, not a migration-in-progress. See
   `docs/decisions/ADR-0001-hexagonal-architecture.md`. Do not propose collapsing feature boundaries
   or reintroducing a shared `controller/service/repository` layer, even for "simple" CRUD.
2. **Kafka and Redis are feature-scoped to `bookmark`, not general infrastructure.** Do not assume
   another feature can use them without first checking `docs/architecture/infrastructure.md` and that
   feature's own doc. Introducing a second consumer of either is fine, but should reuse the existing
   port pattern (`shared.idempotency.application.IdempotencyService` for Redis; a new
   `{feature}.{event}.v1` topic + own domain event for Kafka) rather than inventing a new mechanism.
3. **Best-effort/fail-open is the deliberate tradeoff for idempotency and event publishing**, not a
   bug. See `docs/decisions/ADR-0002-redis-idempotency.md` and
   `docs/decisions/ADR-0003-kafka-domain-events.md`. Do not "fix" a caught-and-logged Redis/Kafka
   failure into a propagated exception without a new ADR justifying the stronger guarantee and its
   cost (e.g. failing bookmark creation when Redis is down).
4. **There is no role-based authorization layer.** `/api/users/**` being reachable by any authenticated
   user is a documented gap (`docs/engineering/security.md`), not a pattern to copy into new code, but
   also not something to silently "fix" with an ad hoc check in one controller — a real authorization
   model is a cross-cutting architectural decision that needs an ADR if introduced.
5. **`models/{Post,Comment,Like}` and `friendship.domain.model.Follow` are intentionally unwired.**
   Do not wire them into a use case/controller as an incidental part of an unrelated change — this is
   scoped, future work (see `docs/development/future-work.md`), not a bug to close opportunistically.

## When a new architectural decision is genuinely needed

Write an ADR (`docs/decisions/ADR-{next}-{slug}.md`, see `docs/decisions/README.md` for the template
and criteria) **before** implementing, when your change:
- Introduces a new architectural style or pattern a future agent might otherwise revert.
- Chooses between two credible alternatives where the reasoning would otherwise be lost.
- Supersedes a previous ADR (mark the old one `Superseded by ADR-000X`, don't delete it).

Do not write an ADR for a routine implementation detail already covered by
`docs/engineering/coding-standards.md` or a feature's own conventions.

## When you find a real architectural problem while working

Document it in `docs/development/known-issues.md` (if it's a concrete, verified gap) rather than
silently fixing it as a side effect of unrelated work — see `docs/ai/development-rules.md` #8.
