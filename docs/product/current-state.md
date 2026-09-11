# Current System State

> Verified against source code, migrations, and git history as of the date below. This is a snapshot,
> not a roadmap — see `.agents/prd/product-backlog.md` for planned work.
>
> **Last verified**: 2026-09-11

## Implemented

| Area | Notes |
|---|---|
| Supabase Auth (signup/login/refresh/logout) | `auth/` — first login creates the local `User` row |
| Structured onboarding / profile stats | `registration/` (multi-step draft → commit) writes into `lifestats/` domain-specific entities: `UserOccupation`, `UserWealth`, `UserPhysique`, `UserWisdom`, `UserCharisma`, `UserSocialAccount`, `UserXfactor`, `ProfileLike`. This **replaced** the earlier generic `StatCategory`/`LifeStat` model — see `docs/known-issues.md`. |
| Profile CRUD, picture upload, profile card, rank progress | `profile/` |
| Scoring (`shared.domain.model.StatDomain`: Occupation/Wealth/Physique/Wisdom/Charisma, weighted) | `scoring/` |
| Ranking (5-tier: Bronze→Elite), leveling/XP, streaks | `ranking/`, `leveling/`, `xp/`, `streak/` |
| Quests, achievements/badges | `quest/`, `achievement/` |
| Friend requests, friendships, profile comparison | `friendship/` (`FriendController`) |
| Leaderboards (overall + category, friends-scoped) | `leaderboard/` |
| Activity types catalog + logging with cooldowns | `activity/` |
| Friend activity feed | `feed/` |
| Cosmetic store (browse/purchase/equip) | `store/` |
| User/general search | `search/` |
| Bookmarks with idempotent create (Redis) + `BookmarkCreated` Kafka event | `bookmark/` — see ADR-0002, ADR-0003 |

## In progress / partially wired

| Item | State |
|---|---|
| `Follow` (follow/unfollow) | Domain model + persistence adapter exist under `friendship/`, but **no use case or controller** wires it to the API. Not usable yet despite living in a fully-hexagonal package. |
| `bookmark` table | Entity + repository exist and are exercised by tests (which use `ddl-auto=update`), but **no Flyway/Supabase migration creates the table** — will fail schema validation in any environment using `ddl-auto=validate`. See `docs/known-issues.md`. |

## Planned / idea (not implemented)

Per `.agents/prd/product-backlog.md`: Post/Comment/Like CRUD (`models/` placeholders only, no
services/controllers/repos), points system, push notifications, trending users. These are tracked in
the backlog, not restated here to avoid drift between two sources.

## Known technical debt

See `docs/known-issues.md` for full detail. Headline items:
- `bookmark` table has no schema migration.
- PITest (`./mvnw pitest:mutationCoverage`) is misconfigured post-migration and currently analyzes
  almost none of the real feature code.
- Several `.agents/skills/*` files reference pre-hexagonal-migration file paths.
- `.agents/skills/life-stats/SKILL.md` describes a data model (`StatCategory`, `LifeStat`,
  `LifeStatHistory`) that has been superseded by structured per-domain stat entities; the underlying
  `life_stats`/`life_stat_history` tables still exist in the schema but are no longer written by
  application code (orphaned).
- `.agents/prd/product-backlog.md` marks some features (e.g. leaderboards, search, achievements,
  friend requests) as `💡 IDEA` even though they are implemented and covered by tests — the backlog
  was not kept in sync with the sprints that shipped them.
- Redis is required for full bookmark idempotency guarantees but is not provisioned in
  `docker-compose.yml`.

## Infrastructure state

- **Local dev**: Supabase CLI (`supabase start`) for Postgres/Auth/Storage; `docker-compose.yml` for
  Kafka (and optionally the app container). Redis must be run separately (not yet in compose).
- **CI**: GitHub Actions (`.github/workflows/ci-cd.yml`) runs `./mvnw clean verify` on push/PR to
  `main`.
- **Deploy**: on push to `main` after CI passes, deploys to Railway via `railway up` (`railway.toml`,
  `Dockerfile`).
