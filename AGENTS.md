# AGENTS.md — Onnyth Server AI Entry Point

> Read this file first. It is the durable, load-frequently entry point for any AI agent working in this
> repository. It links out to deeper context instead of duplicating it — follow the links for the task
> at hand instead of loading everything.

## Project identity

**Onnyth** is a gamified life-tracking platform. Users build a profile, complete a structured
onboarding flow to record real-world stats (occupation, wealth, physique, wisdom, charisma), earn a
weighted score and XP, progress through ranks/levels, complete quests, unlock achievements, buy
cosmetics, and interact with friends (requests, leaderboards, activity feed). This repository is the
**backend only**: a single Spring Boot REST API module. There is no monorepo/frontend code here.

## Repository map

```text
AGENTS.md                → you are here (AI entry point)
docs/                     → the durable, single source of truth for product, architecture, features,
                            API, data, engineering standards, ADRs, and current implementation state
src/main/java/…           → application source, organized per-feature (see Architecture below)
src/main/resources/db/migration/ → Flyway migrations (incomplete — see docs/data/overview.md)
supabase/migrations/      → Supabase CLI migrations (parallel migration history, see docs/development/known-issues.md)
src/test/java/…           → tests, package structure mirrors src/main/java per feature
docker-compose.yml        → local Kafka broker (+ app container); Postgres/Auth/Storage come from `supabase start`
.github/workflows/ci-cd.yml → CI (mvn verify) + Railway deploy on push to main
```

`docs/` is the **only** documentation/process system in this repository — see `docs/README.md` for its
full structure. Two prior systems have been retired and fully consolidated into `docs/`: a per-feature
skills tree (`.agents/skills/*/SKILL.md`, now `docs/features/` and `docs/architecture/`) and a
product/process tree (`.agents/prd/` — product backlog, sprint plans, user stories — now
`docs/development/`). Do not recreate either as a parallel system.

## Technology stack

| Layer | Technology |
|---|---|
| Language / Framework | Java 21, Spring Boot 3.5.3 |
| Database | PostgreSQL, hosted on Supabase; Flyway migrations; `ddl-auto=validate` in prod |
| Auth | Supabase Auth, validated as an OAuth2 Resource Server (JWT) |
| File storage | Supabase Storage |
| Cache / idempotency store | Redis (`spring-boot-starter-data-redis`) — currently used only by the `bookmark` feature |
| Messaging | Kafka (`spring-kafka`), local broker via `docker-compose.yml` — currently used only by the `bookmark` feature |
| API docs | SpringDoc OpenAPI / Swagger UI at `/swagger-ui.html`, `/api-docs` |
| Testing | JUnit 5, Testcontainers (Postgres), WireMock (Supabase), PITest (mutation testing) |
| Build / Deploy | Maven, GitHub Actions → Railway |

## Architecture: Hexagonal (per-feature)

This codebase was fully migrated from a top-level `controller/service/repository/models` layout to a
**per-feature hexagonal layout** (see `docs/architecture/low-level-design.md` and
`docs/decisions/ADR-0001-hexagonal-architecture.md`). Every feature package should follow this shape:

```text
{feature}/
├── domain/model              # framework-free POJOs / enums — no JPA, no Spring
├── domain/event               # feature-owned domain events (when the feature publishes events)
├── application/port          # outbound interfaces (repository ports, publisher ports)
├── application/usecase       # business logic ("...UseCaseService"), orchestrates ports
├── application/exception      # feature-specific exceptions extending shared.exception.ApiException
├── adapter/in/rest           # controllers + request/response DTOs (records)
├── adapter/in/kafka          # inbound Kafka consumers (if the feature consumes events)
├── adapter/out/persistence   # JPA entities, Spring Data repos, mappers, port implementations
└── adapter/out/kafka         # outbound Kafka publishers/topic constants (if the feature publishes events)
```

Current feature modules: `achievement`, `activity`, `auth`, `bookmark`, `feed`, `friendship`,
`leaderboard`, `leveling`, `lifestats`, `profile`, `quest`, `ranking`, `registration`, `scoring`,
`search`, `store`, `streak`, `user`, `xp`. Cross-cutting code lives in `shared` (base exceptions,
`StatDomain`, idempotency contracts), `configuration`, `security`, `system`. `models/` holds
`Post`/`Comment`/`Like` — intentionally unwired placeholders for a future social feature (see
`docs/development/future-work.md`).

### Rules future agents MUST follow

1. **Preserve this hexagonal shape.** Do not reintroduce top-level `controller/`, `service/`,
   `repository/`, or `models/` packages for new work.
2. **Dependency direction**: `adapter → application → domain`. Domain must never import Spring,
   JPA, or adapter classes. Application depends on domain + its own ports, never on a concrete adapter.
3. **Controllers only translate.** REST/Kafka inbound adapters parse input, call a use case, map the
   result to a DTO/response. No business logic in controllers.
4. **Persistence adapters implement application ports.** Never call a `*JpaRepository` from a
   controller or another feature directly — go through the port.
5. **Cross-feature access goes through the owning feature's port**, not its entity/repository. If two
   features need the same concept, consider whether it belongs in `shared`.
6. **New port/adapter only if genuinely needed.** Don't add an interface with one implementation "for
   future flexibility" — most features don't need a strategy abstraction.
7. **Transactions**: mark use-case write methods `@Transactional`. Publish events (Kafka) via
   `TransactionSynchronizationManager.registerSynchronization(...).afterCommit()` when a use case both
   writes to the DB and publishes an event in the same call — see
   `bookmark/application/usecase/BookmarkUseCaseService.java` for the reference pattern.
8. **Errors**: throw a feature-specific subclass of `shared.exception.ApiException` from
   `application/exception`. `shared.exception.GlobalExceptionHandler` handles all `ApiException`s
   generically; add a feature-scoped `@RestControllerAdvice(assignableTypes = FooController.class)`
   only if a feature needs custom mapping beyond the generic handler (see
   `bookmark/adapter/in/rest/BookmarkExceptionHandler.java`).
9. **DTOs are Java records** living next to the adapter that owns them
   (`adapter/in/rest/dto/`), with Jakarta Validation annotations and static `fromDomain(...)` factories.
10. **New Flyway migration**: `V{next}__description.sql` in `src/main/resources/db/migration/`; check
    the highest existing `V{N}` first (see `docs/data/database-schema.md`). Every entity that
    will run against production (`ddl-auto=validate`) needs a matching migration **and** a mirrored
    `supabase/migrations/*.sql` entry — do not rely on the test profile's `ddl-auto=update` to paper
    over a missing migration (see `docs/development/known-issues.md` #1 for the current, large-scale
    instance of this gap — roughly half the system's tables have no Flyway migration today).
11. **`/api/v1/` prefix** for new endpoints unless following an existing unprefixed contract
    (`/api/users/**`).

## Development workflow

```bash
./mvnw compile                       # build
./mvnw test                          # all tests
./mvnw test -Dtest="com.onnyth.onnythserver.bookmark.**"   # single feature
./mvnw pitest:mutationCoverage       # mutation testing (currently misconfigured, see docs/engineering/testing.md)
./mvnw spring-boot:run               # run locally (needs `supabase start` for DB/Auth/Storage + Kafka)
docker compose up                    # local Kafka broker (+ optional containerized app)
```

Local dependencies: Supabase CLI (`supabase start` for Postgres/Auth/Storage) + `docker-compose.yml`
for Kafka. Redis must also be running locally (`localhost:6379`) for the bookmark idempotency feature
to work — `docker-compose.yml` does not currently start Redis (see `docs/development/known-issues.md`).

## Documentation workflow

Before a significant change: read `docs/ai/context.md`, then the relevant `docs/features/<feature>.md`,
`docs/development/current-state.md` for status, and `docs/decisions/README.md` for any ADR governing
the area you're touching.

After a change, update docs if it affected: architecture/boundaries, business rules, API contracts,
DB schema, events/Kafka topics, Redis usage, auth/authorization, infra/deployment, or testing strategy.
Full protocol: `docs/ai/documentation-rules.md`. Concretely:
- Feature behavior/endpoints/entities changed → update the matching `docs/features/<feature>.md`
  (replace sections in place) and `docs/features/README.md` if its status changed.
- A new significant architectural decision (or one that reverses a prior ADR) → add/supersede an ADR
  under `docs/decisions/`.
- Implemented/planned status changed → update `docs/development/current-state.md`.
- A project-wide AI/dev rule changed → update this file.

Do not update docs for pure refactors with no behavior/contract change.

## Known constraints / important notes

- **`docs/` is the sole documentation/process system.** Two prior systems (`.agents/skills/*/SKILL.md`
  and `.agents/prd/`) have been retired and fully consolidated into `docs/` — do not recreate either.
  See `docs/README.md`.
- **Several product behaviors that look wired are not actually triggered end-to-end** — most
  importantly, score recalculation, feed-event creation, and broad achievement re-evaluation all have
  the code/contract in place but no caller/listener invokes them. See
  `docs/development/known-issues.md` #4, #6, #7 and `docs/development/current-state.md` before
  assuming these behaviors work.
- **Kafka/Redis are feature-scoped**, not general infra — only `bookmark` uses them today. Don't
  assume other features have an event bus or cache.
- **Roughly half the system's tables have no Flyway migration** (including `bookmark`) — they work
  only because the test profile uses `ddl-auto=update` and because production's actual schema was
  bootstrapped via Supabase-side migrations. See `docs/development/known-issues.md` #1 and
  `docs/data/overview.md`.
- Secrets (`SUPABASE_*`, `DATABASE_*`, `RAILWAY_TOKEN`) are supplied via environment/`.env`/CI
  secrets — never hardcode or print real values in docs or code.
