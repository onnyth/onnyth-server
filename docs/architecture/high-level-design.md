# High-Level Design

This document describes the major pieces of the system and how they're organized — not individual
class details (see `docs/architecture/low-level-design.md` for that).

## System boundary

This repository is the **backend only**: a single Spring Boot REST API module (`onnyth-server`).
There is no monorepo/frontend code here. It is consumed by a separate mobile/web client (not in this
repository) over HTTPS/JSON.

## Major building blocks

```text
┌─────────────────────────────────────────────────────────────────────────┐
│ Onnyth Server (Spring Boot 3.5.3, Java 21)                              │
│                                                                          │
│  ┌──────────────┐   ┌───────────────────────────────────────────────┐  │
│  │ security/    │   │ Feature modules (per-feature hexagon)         │  │
│  │ SecurityConfig│  │ auth, user, profile, registration, lifestats, │  │
│  │ (JWT resource │  │ scoring, ranking, leveling, xp, streak,       │  │
│  │  server)      │  │ friendship, leaderboard, feed, search,        │  │
│  └──────────────┘   │ achievement, quest, activity, store, bookmark │  │
│                     └───────────────────────────────────────────────┘  │
│  ┌──────────────┐   ┌───────────────┐   ┌────────────────────────────┐ │
│  │ shared/       │   │ configuration/│   │ system/                    │ │
│  │ ApiException,│   │ RestTemplate  │   │ HealthController            │ │
│  │ StatDomain,  │   │ bean, etc.    │   │                            │ │
│  │ idempotency  │   └───────────────┘   └────────────────────────────┘ │
│  └──────────────┘                                                       │
└─────────────────────────────────────────────────────────────────────────┘
        │                    │                    │                │
        ▼                    ▼                    ▼                ▼
  PostgreSQL           Supabase Auth        Supabase Storage      Redis + Kafka
  (Supabase-hosted,    (external HTTP,      (external HTTP,       (bookmark feature
  Flyway-migrated)     via auth/)           via profile/)         only, see below)
```

## Feature boundaries and ownership

Each feature module owns its own domain model, use cases, and persistence — no feature owns another
feature's database table directly. The current feature set (19 modules) is grouped here by product
area for orientation; each has its own `docs/features/*.md`:

| Product area | Feature modules |
|---|---|
| Identity & account | `auth`, `user` |
| Profile & presentation | `profile`, `registration` |
| Real-world stats | `lifestats` |
| Gamification core | `scoring`, `ranking`, `leveling`, `xp`, `streak` |
| Social | `friendship`, `leaderboard`, `feed`, `search` |
| Progression content | `achievement`, `quest`, `activity`, `store` |
| Utility / reference pattern | `bookmark` |

Cross-cutting, non-feature-owned code lives in `shared` (base exceptions, `StatDomain`,
`StatChangedEvent`, idempotency contracts), `configuration` (application-wide beans), `security`
(the one global filter chain), and `system` (health endpoint). `models/` holds `Post`/`Comment`/`Like`
— intentionally unwired placeholders for a possible future social feature (see
`docs/development/future-work.md`).

## External systems

| System | Role | Owning feature(s) |
|---|---|---|
| **Supabase Auth** | Identity provider (signup/login/refresh/logout, JWT issuance) | `auth` |
| **Supabase Storage** | Profile picture storage | `profile` |
| **PostgreSQL (Supabase-hosted)** | System of record for every feature's tables | all |
| **Redis** | Idempotency-key cache (best-effort) | `bookmark` only today |
| **Kafka** | Domain event bus (best-effort, async relative to the HTTP response) | `bookmark` only today |

**Kafka and Redis are feature-scoped, not general infrastructure** — only `bookmark` uses them. Do not
assume another feature has an event bus or cache without checking its `docs/features/*.md` file.

## Communication patterns

- **Client ↔ server**: synchronous REST/JSON over HTTPS, one request per business operation.
- **Server ↔ Supabase Auth/Storage**: synchronous outbound HTTP (via a shared `RestTemplate` bean),
  server acting as a thin authenticated proxy.
- **Cross-feature, in-process**: either (a) a direct dependency on another feature's `application/port`
  interface + `domain/model` (the common case — e.g. `profile` reading `lifestats` ports), or (b) a
  Spring `ApplicationEvent` (`StatChangedEvent`, consumed by `scoring`) for one specific
  write-triggers-recalculation flow.
- **Cross-feature, async/decoupled**: Kafka domain events — currently only `bookmark.created.v1`.

## Data ownership

Every table is owned by exactly one feature's persistence adapter; no two features write the same
table. Where a feature needs data owned by another (e.g. `profile` needs `lifestats` domain scores),
it depends on that feature's port, never queries its table directly. See
`docs/data/relationships.md` for the full data-ownership map.
