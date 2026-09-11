---
name: onnyth-skills
description: Master index of all Onnyth Server agent skills — read this first to understand what knowledge is available
---

# Onnyth Server — Skills Index

> **Read this file first.** It is the entry point to all codified knowledge about the Onnyth Server codebase.

## Registered Skills

| # | Skill | Path | Summary |
|---|---|---|---|
| 1 | **Project Overview** | [onnyth-overview/SKILL.md](onnyth-overview/SKILL.md) | Tech stack, architecture, package structure, social feature status |
| 2 | **Authentication** | [authentication/SKILL.md](authentication/SKILL.md) | Supabase Auth flows, JWT security, login-creates-user pattern |
| 3 | **User Profile** | [user-profile/SKILL.md](user-profile/SKILL.md) | Profile CRUD, picture upload, profile card, completion logic |
| 4 | **Life Stats** | [life-stats/SKILL.md](life-stats/SKILL.md) | 5 categories, single/bulk input, history tracking, event flow |
| 5 | **Scoring & Ranking** | [scoring-and-ranking/SKILL.md](scoring-and-ranking/SKILL.md) | Weighted score formula, event-driven recalculation, 5-tier ranks |
| 6 | **Data Layer** | [data-layer/SKILL.md](data-layer/SKILL.md) | All JPA entities, repositories, Flyway V1–V5, database schema |
| 7 | **Error Handling** | [error-handling/SKILL.md](error-handling/SKILL.md) | ApiException hierarchy, GlobalExceptionHandler, error response format |
| 8 | **Testing** | [testing/SKILL.md](testing/SKILL.md) | Test structure, Testcontainers, WireMock, PITest, support classes |
| 9 | **API Reference** | [api-reference/SKILL.md](api-reference/SKILL.md) | All REST endpoints, methods, auth requirements, DTOs |
| 10 | **Conventions** | [conventions/SKILL.md](conventions/SKILL.md) | Coding patterns, naming rules, DTO/service/controller standards |
| 11 | **Bookmark** | [bookmark/SKILL.md](bookmark/SKILL.md) | Bookmark CRUD; reference pattern for Redis idempotency + Kafka domain events |

## Skill File Format

Every skill is a `SKILL.md` inside its own subdirectory under `.agents/skills/`:

```
.agents/skills/
└── <skill-name>/
    └── SKILL.md      ← YAML frontmatter (name, description) + detailed markdown
```

## How to Use Skills

1. **Before any work**: Read this index to identify which skills are relevant
2. **During planning**: Read the specific skill files for the components you'll touch
3. **After completing work**: Update affected skills per the [Skill Update Protocol](../prd/ORCHESTRATOR.md#skill-update-protocol)

## Adding a New Skill

1. Create `.agents/skills/<skill-name>/SKILL.md` with YAML frontmatter
2. Add a row to the table above
3. Keep the skill focused on **one feature area** — split if it grows beyond ~200 lines

## Current Coverage

- **Fully documented**: Auth, Profiles, Scoring, Ranking, Data Layer, Errors, Tests, API, Conventions, Bookmark
- **Stale — verify against source before trusting file paths**: `authentication`, `error-handling`,
  `testing` predate the hexagonal migration (paths patched in this pass, but re-verify if they drift).
  `life-stats/SKILL.md` describes a superseded data model (`StatCategory`/`LifeStat`) — the real
  model is domain-specific stat entities under `lifestats/` driven by `registration/`'s onboarding
  flow. See `docs/known-issues.md` before relying on `life-stats/SKILL.md`.
- **Flagged as not-yet-implemented**: Post/Comment/Like (models exist, no services/controllers).
  `Follow` has a persistence adapter under `friendship/` but no use case/controller — still not
  user-facing. See `docs/product/current-state.md`.
