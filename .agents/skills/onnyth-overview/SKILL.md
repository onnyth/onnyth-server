---
name: onnyth-overview
description: Project overview, tech stack, architecture, and package structure for the Onnyth Server backend
---

# Onnyth Server — Project Overview

Onnyth is a **gamified life-tracking platform** where users build an Onnyth profile, track life stats, earn weighted scores, and progress through RPG-style ranks. The backend is a Spring Boot REST API backed by Supabase (PostgreSQL + Auth + Storage).

## Tech Stack

| Layer | Technology | Version |
|---|---|---|
| Language | Java | 21 |
| Framework | Spring Boot | 3.5.3 |
| Database | PostgreSQL (Supabase-hosted) | — |
| ORM | Spring Data JPA / Hibernate | — |
| Auth | Supabase Auth → Spring OAuth2 Resource Server (JWT) | — |
| File Storage | Supabase Storage | — |
| Migrations | Flyway | — |
| Build | Maven | — |
| API Docs | SpringDoc OpenAPI / Swagger UI | 2.8.4 |
| Cache / Idempotency | Redis (`spring-boot-starter-data-redis`) — used by `bookmark` only, see `docs/decisions/ADR-0002-redis-idempotency.md` | — |
| Messaging | Kafka (`spring-kafka`), local broker via `docker-compose.yml` — used by `bookmark` only, see `docs/decisions/ADR-0003-kafka-domain-events.md` | — |
| Testing | JUnit 5, Testcontainers, WireMock, PITest | — |
| Utilities | Lombok, spring-dotenv | — |

## Top-Level Package Structure

```text
com.onnyth.onnythserver
├── achievement   # badges, unlock progress, displayed badges
├── activity      # activity logging and activity types
├── auth          # Supabase-backed signup/login/refresh flows
├── bookmark      # user bookmark CRUD
├── friendship    # friend requests, friendships, profile comparison
├── feed          # activity/feed timeline events
├── leaderboard   # overall and category leaderboards
├── lifestats      # domain-specific stat entities (occupation, wealth, physique, wisdom, charisma, social accounts) — see Notes
├── profile        # profile reads/updates, votes, media upload
├── quest          # quests and quest completions
├── ranking        # rank tiers and rank progress
├── registration   # multi-step onboarding draft + commit flow — writes into lifestats entities
├── scoring        # domain score recalculation and score history (shared.domain.model.StatDomain)
├── search        # user and general search endpoints
├── store         # cosmetics, purchases, equips
├── streak        # streak tracking
├── user          # core user aggregate and account deletion
├── xp            # XP awarding workflow
├── leveling      # level progression and level-up events
├── shared        # shared kernel and cross-cutting support
├── system        # framework/system endpoints such as health
├── models        # placeholder social models kept intentionally (Post/Comment/Like)
├── configuration # application-wide bean/config packages
└── security      # cross-cutting security configuration
```

## Standard Feature Module Layout

Most feature packages use the same hexagonal layout:

```text
{feature}/
├── domain/model              # plain POJOs / enums
├── domain/event              # feature-owned domain events (when needed)
├── application/port          # repository and outbound port interfaces
├── application/usecase       # business logic services
├── application/exception     # feature-specific ApiException subclasses
├── adapter/in/rest           # controllers and REST DTOs
└── adapter/out/persistence   # JPA entities, Spring Data repos, mappers, adapters
```

## Shared Kernel

`com.onnyth.onnythserver.shared` holds cross-cutting concerns used across module boundaries, including the `ApiException` base class, the shared `StatDomain` enum, and the shared `StatChangedEvent` contract.

## Architecture Flow

Client → REST adapter → application use case → application port → persistence adapter → PostgreSQL/Supabase.

## Notes

- Registration is its own bounded context and orchestrates onboarding into user + `lifestats` data
  (`UserOccupation`, `UserWealth`, `UserPhysique`, `UserWisdom`, `UserCharisma`, `UserSocialAccount`,
  `UserXfactor`, `ProfileLike`). This structured model **replaced** an earlier generic
  `StatCategory`/`LifeStat` model; `.agents/skills/life-stats/SKILL.md` still describes the old model
  — see `docs/known-issues.md` before trusting it.
- Scoring keys off `shared.domain.model.StatDomain` (`OCCUPATION`/`WEALTH`/`PHYSIQUE`/`WISDOM`/`CHARISMA`,
  each with a weight), not the old `StatCategory` enum.
- `bookmark` is the newest module and the reference implementation for Redis-backed idempotency and
  Kafka domain events — neither is used by any other feature today. See
  `.agents/skills/bookmark/SKILL.md` and `docs/decisions/ADR-0002-redis-idempotency.md` /
  `docs/decisions/ADR-0003-kafka-domain-events.md`.
- `models/Post`, `models/Comment`, and `models/Like` remain as intentionally unwired placeholders for a
  future social feature. `Follow` (under `friendship/domain/model/`) also exists with a persistence
  adapter but no use case/controller — still not user-facing.
