# Architecture Overview

## At a glance

```text
Client (mobile/web app)
   │  HTTPS + Supabase-issued JWT (Bearer token)
   ▼
Spring Security filter chain (OAuth2 Resource Server, JWT validation)
   ▼
REST controller           adapter/in/rest        — parses input, calls a use case, maps to a DTO
   ▼
Use case service          application/usecase    — business logic, orchestrates ports
   ▼
Outbound port (interface) application/port        — repository / publisher contracts
   ▼
Adapter                   adapter/out/persistence, adapter/out/kafka, adapter/out/storage
   ▼
PostgreSQL (Supabase) │ Redis │ Kafka │ Supabase Storage │ Supabase Auth (external HTTP)
```

Every feature module is a self-contained vertical slice ("hexagon") through this stack. There is no
shared top-level `controller/`, `service/`, or `repository/` layer — see
`docs/architecture/module-structure.md`.

## Architectural style

**Per-feature hexagonal (ports & adapters) architecture.** The codebase was deliberately migrated,
one feature at a time, from a single flat `controller/service/repository/models` layout to this shape
(see `docs/decisions/ADR-0001-hexagonal-architecture.md` for the full migration history). The
motivation recorded in the codebase: enforce a consistent per-feature boundary and keep
framework/persistence concerns out of domain and application code.

## Core principles

1. **Domain has no framework dependencies.** `domain/model` classes are plain Java — no JPA, no Spring
   annotations (a few domain models are intentionally mutable POJOs with Lombok `@Builder`/`@Getter`/
   `@Setter` for pragmatic reasons — Lombok itself is not a runtime framework dependency).
2. **Dependency direction is one-way**: `adapter → application → domain`. Domain never imports
   application or adapter code. Application depends only on domain and its own `port` interfaces, never
   on a concrete adapter class.
3. **Controllers only translate.** No business logic in `adapter/in/rest` — parse request, call a use
   case, map the result to a response DTO.
4. **Persistence is behind a port.** No feature or controller calls another feature's
   `*JpaRepository` directly; it goes through that feature's `application/port` interface.
5. **Cross-feature reuse is a compile-time dependency on a domain model or a port**, not a shared
   database table accessed twice. E.g. `profile/` depends on `lifestats.application.port.*` and
   `user.domain.model.User`, not on raw SQL against `lifestats`' tables.

## What "hexagonal" buys this codebase today

- **Locate all code for a feature in one package subtree.**
- **Swap an adapter without touching business logic** — e.g. `bookmark`'s Redis idempotency and Kafka
  publishing are adapters behind ports; a future feature could reuse the same ports with different
  infrastructure.
- **Unit-test use cases without a Spring context** (mock the ports, not a database).

## What it costs

- Cross-feature reuse requires going through a port interface — one extra layer of indirection versus
  calling a repository directly, even for a simple read.
- Several features (`profile/`, `feed/`, `achievement/`) end up depending on many other features'
  ports simultaneously to assemble a single read-model (see `docs/features/profiles.md`,
  `docs/features/feed.md`) — this is an accepted tradeoff of a rich, cross-cutting read-model, not a
  violation of the architecture, since each dependency is still a port, not a raw repository call.

## Related documents

- `docs/architecture/module-structure.md` — the exact package shape and current feature list
- `docs/architecture/dependency-rules.md` — the enforceable rules for new code
- `docs/architecture/request-flow.md` — a concrete request walked end-to-end
- `docs/architecture/infrastructure.md` — runtime dependencies (Postgres, Redis, Kafka, Supabase)
- `docs/decisions/ADR-0001-hexagonal-architecture.md` — why/when this was adopted
