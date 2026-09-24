# Current System State

> Verified against source code, migrations, and git history. This is a snapshot, not a roadmap — see
> `docs/development/future-work.md` for planned work. Per-feature detail and full verification lives
> in each `docs/features/*.md` — this file is a summary; if it drifts from a feature doc, the feature
> doc wins.
>
> **Last verified**: 2026-09-23

## Implemented and working end-to-end

| Area | Notes |
|---|---|
| Supabase Auth (signup/login/refresh/logout) | `auth/` — first login creates the local `User` row |
| Structured onboarding | `registration/` (multi-step draft → commit) writes into `lifestats/`'s domain-specific entities |
| Profile CRUD, picture upload, profile card, profile votes | `profile/` |
| Rank tier display + progress calculation | `ranking/` (see caveats below — not auto-refreshed) |
| Leveling / XP from activities, quests, streaks | `leveling/`, `xp/` |
| Streaks with milestone XP bonuses | `streak/` |
| Quests (catalog read + one-time completion) | `quest/` (catalog starts empty — no seed data) |
| Friend requests, friendships, friend profile comparison | `friendship/` |
| Leaderboards (overall + category, friends-scoped only) | `leaderboard/` |
| Activity types catalog + logging with cooldowns | `activity/` |
| Cosmetic store (browse/purchase/equip) | `store/` (currency/rarity data has drifted — see below) |
| User/general search | `search/` |
| Bookmarks with idempotent create (Redis) + `BookmarkCreated` Kafka event | `bookmark/` — missing schema migration, see below |
| Achievement catalog + badge display | `achievement/` (unlock evaluation trigger is narrow — see below) |

## Implemented but functionally incomplete (the most important corrections vs. earlier docs)

These are **not** minor gaps — each one means a described product behavior does not actually happen
yet, despite the surrounding code looking complete. Full detail in each linked feature doc and
`docs/development/known-issues.md`.

| Behavior everyone would assume works | Actual state |
|---|---|
| "Updating a life stat recalculates your score" | **Does not happen.** `ScoreCalculationUseCaseService.recalculateAll(...)` has no call sites at all. See `docs/features/scoring.md`, known issue #4. |
| "Your rank tier updates when your score changes" | Only updated from one call site, inside quest completion — not from registration or score recalculation. See `docs/features/ranking.md`, known issue #5. |
| "Friends see my activity/level-ups/achievements/streaks in their feed" | **Never happens.** `FeedUseCaseService.createFeedEvent(...)` has zero callers. See `docs/features/feed.md`, known issue #6. |
| "Achievements unlock automatically as I make progress" | Only re-evaluated at one trigger point: accepting a friend request. See `docs/features/achievements.md`, known issue #7. |
| "Leveling up notifies something" | `LevelUpEvent` is published but has no listeners. See known issue #8. |

## Partially wired

| Item | State |
|---|---|
| `Follow` (follow/unfollow) | Domain model + persistence adapter exist under `friendship/`, but no use case/controller wires it to the API. See known issue #9. |
| `bookmark` table (+ 13 other tables) | Entities/repositories exist and are exercised by tests, but have no Flyway migration — will fail schema validation in a Flyway-only bootstrap. See known issue #1. |
| `registration_drafts` expiry cleanup | Query method exists, nothing calls it. See known issue #10. |

## Planned / idea (not implemented)

Post/Comment/Like CRUD (`models/` placeholders only), Follow/unfollow as a real feature, a points
system, push notifications, trending users, weekly stat reminders. See
`docs/development/future-work.md`.

## Known technical debt

See `docs/development/known-issues.md` for the full, evidence-based list (24 numbered items as of this
writing). Headline categories: a large schema-migration gap between Flyway and the actual Supabase
schema; several "publishes/exposes a hook but nothing consumes it" gaps (score recalculation, feed
events, level-up events, achievement re-evaluation); seed-data drift from current enums
(achievements, activity types, cosmetics); a stale, unwired PITest configuration; no role-based
authorization.

## Infrastructure state

- **Local dev**: Supabase CLI (`supabase start`) for Postgres/Auth/Storage; `docker-compose.yml` for
  Kafka (and optionally the app container). Redis must be run separately (not yet in compose).
- **CI**: GitHub Actions (`.github/workflows/ci-cd.yml`) runs `./mvnw clean verify` on push/PR to
  `main`.
- **Deploy**: on push to `main` after CI passes, deploys to Railway via `railway up`.

See `docs/architecture/infrastructure.md` and `docs/engineering/deployment.md` for full detail.
