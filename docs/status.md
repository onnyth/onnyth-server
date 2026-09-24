# System Status

> Dashboard-style summary. Every row links to the authoritative doc — if this table and a linked doc
> disagree, the linked doc is correct; fix this file to match.
>
> **Last updated**: 2026-09-23

## Product features

| Area | Status | Detail |
|---|---|---|
| Authentication | ✅ Implemented | [`docs/features/authentication.md`](features/authentication.md) |
| Users | ✅ Implemented | [`docs/features/users.md`](features/users.md) |
| Profiles | ✅ Implemented | [`docs/features/profiles.md`](features/profiles.md) |
| Registration / Onboarding | ✅ Implemented* | [`docs/features/registration.md`](features/registration.md) — *profile-completion bypass, see doc |
| Life Stats | 🚧 Partially Implemented | [`docs/features/life-stats.md`](features/life-stats.md) — no dedicated CRUD surface |
| Scoring | 🚧 Partially Implemented | [`docs/features/scoring.md`](features/scoring.md) — **recalculation is not wired to any trigger** |
| Ranking | 🚧 Partially Implemented | [`docs/features/ranking.md`](features/ranking.md) — not auto-refreshed; `ELITE` tier unreachable from current formula |
| Leveling | ✅ Implemented | [`docs/features/leveling.md`](features/leveling.md) |
| XP | ✅ Implemented | [`docs/features/xp.md`](features/xp.md) |
| Streaks | 🚧 Partially Implemented | [`docs/features/streak.md`](features/streak.md) — no feed/event hook |
| Quests | ✅ Implemented | [`docs/features/quests.md`](features/quests.md) — empty catalog, no seed data |
| Achievements | 🚧 Partially Implemented | [`docs/features/achievements.md`](features/achievements.md) — narrow unlock trigger, seed drift |
| Activities | 🚧 Partially Implemented | [`docs/features/activities.md`](features/activities.md) — seed category drift |
| Cosmetics | 🚧 Partially Implemented | [`docs/features/cosmetics.md`](features/cosmetics.md) — currency/rarity/seed drift |
| Friendships | 🚧 Partially Implemented | [`docs/features/friendships.md`](features/friendships.md) — `Follow` unwired |
| Leaderboard | ✅ Implemented | [`docs/features/leaderboard.md`](features/leaderboard.md) — friends-scoped only |
| Feed | 🚧 Partially Implemented | [`docs/features/feed.md`](features/feed.md) — **no producer ever creates an event** |
| Search | ✅ Implemented | [`docs/features/search.md`](features/search.md) |
| Bookmarks | ✅ Implemented† | [`docs/features/bookmarks.md`](features/bookmarks.md) — †missing schema migration, see below |
| Posts / Comments / Likes / Follow-as-a-feature | 📋 Planned | JPA placeholders only, see [`docs/development/future-work.md`](development/future-work.md) |

## Architecture

| Area | Status |
|---|---|
| Per-feature hexagonal structure | ✅ Fully migrated (all 19 feature modules) |
| Dependency-direction rules enforced by convention | ✅ (no automated linter) |
| Event-driven cross-feature triggers (score, feed, achievements, level-up) | 🚧 Contracts/events exist; most producer↔consumer wiring is incomplete — see [`docs/development/known-issues.md`](development/known-issues.md) |
| Kafka domain events | 🚧 One feature only (`bookmark`) |
| Redis idempotency | 🚧 One feature only (`bookmark`); not provisioned in local `docker-compose.yml` |

## Infrastructure

| Component | Status |
|---|---|
| PostgreSQL (Supabase-hosted) | ✅ |
| Supabase Auth | ✅ |
| Supabase Storage | ✅ |
| Flyway migrations | 🚧 Incomplete — covers only ~half the real schema, see [`docs/development/known-issues.md`](development/known-issues.md) #1 |
| Redis | 🚧 Used by one feature; not in `docker-compose.yml` |
| Kafka | 🚧 Used by one feature; local broker only, no production provisioning evidenced |
| CI (GitHub Actions) | ✅ `./mvnw clean verify` on push/PR to `main` |
| CD (Railway) | ✅ auto-deploy on push to `main` after CI passes |
| Mutation testing (PITest) | 🚧 Configured but stale — analyzes almost no real code |

## Documentation system

| Area | Status |
|---|---|
| `docs/` as the single source of truth (this consolidation) | ✅ Complete — `.agents/skills/` and `.agents/prd/` both retired, see `docs/README.md` |
| Every feature has a verified `docs/features/*.md` | ✅ 19/19 |
| ADRs for major architectural decisions | ✅ `docs/decisions/` |
