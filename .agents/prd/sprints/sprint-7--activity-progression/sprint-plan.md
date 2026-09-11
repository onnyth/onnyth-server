# Sprint Plan — Sprint 7: Activity & Progression System

> **Goal**: Build the activity logging system, XP/level progression, daily streaks, activity feed, and cosmetic store
> **Status**: `COMPLETED`
> **Source**: [Backend Brief](backend-brief.md)
> **Client Sprints**: Sprint 6, 7, 8
> **Source PRD**: PRD-005

## Context

Client Sprints 6–8 are building the Activity System, Progression UI, Social Feed, and Cosmetic Store. Backend must provide all data models, services, and API endpoints. The existing Quest system coexists — Quests = system challenges, Activities = user-logged daily actions.

> [!IMPORTANT]
> Migrations start at V14 (V13 was the last in Sprint 6).

## Sprint Scope

| # | Story | Client Stories | Priority | Status |
|---|---|---|---|---|
| S7-01 | Data Model — ActivityType, ActivityLog, UserStreak, FeedEvent, Cosmetics entities + enums + migrations | All | 🔴 Must | ✅ |
| S7-02 | Activity Types — Repo, seed data, GET endpoint | S-031, S-032 | 🔴 Must | ✅ |
| S7-03 | XP & Level System — XpService, LevelService, User entity changes | S-036 | 🔴 Must | ✅ |
| S7-04 | Activity Logging — ActivityService, POST /log, GET /history, GET /status | S-033 | 🔴 Must | ✅ |
| S7-05 | Streak System — StreakService + GET /streaks endpoint | S-040 | 🔴 Must | ✅ |
| S7-06 | Profile Card Extension — Add level, title, streak to profile card | S-037 | 🔴 Must | ✅ |
| S7-07 | Activity Feed — FeedService + GET /feed endpoint | S-043, S-044 | 🔴 Must | ✅ |
| S7-08 | Cosmetic Store — CosmeticService + store endpoints | S-047, S-049 | 🟡 Should | ✅ |

## Stories

- [x] [S7-01 — Data Model](stories/S7-01--data-model.md)
- [x] [S7-02 — Activity Types](stories/S7-02--activity-types.md)
- [x] [S7-03 — XP & Level System](stories/S7-03--xp-level-system.md)
- [x] [S7-04 — Activity Logging](stories/S7-04--activity-logging.md)
- [x] [S7-05 — Streak System](stories/S7-05--streak-system.md)
- [x] [S7-06 — Profile Card Extension](stories/S7-06--profile-card-extension.md)
- [x] [S7-07 — Activity Feed](stories/S7-07--activity-feed.md)
- [x] [S7-08 — Cosmetic Store](stories/S7-08--cosmetic-store.md)

## Ordering Rationale

1. **S7-01 (Data Model)** — All entities, enums, migrations, DTOs, repos. Foundation
2. **S7-02 (Activity Types)** — Seed the activity catalog. Blocks all client activity UI
3. **S7-03 (XP & Level)** — XpService + LevelService needed before logging can award XP
4. **S7-04 (Activity Logging)** — Core feature: log activities, award XP, check cooldowns
5. **S7-05 (Streaks)** — StreakService integrated with activity logging
6. **S7-06 (Profile Card)** — Extend existing endpoint with new level/streak data
7. **S7-07 (Feed)** — FeedService creates events from activity logs, level-ups, streaks
8. **S7-08 (Cosmetics)** — Lower priority store feature

## Sprint Notes

- Migrations V14–V20 (V13 was last in Sprint 6)
- XP is a **separate field** from `totalScore`. `totalScore` = weighted stat sum + quest XP. `xp` = activity XP
- Level XP curve: `xpForLevel(n) = 100 + floor((n-1) / 5) * 50` (progressive, server-computed)
- Level titles: Novice (1–4), Apprentice (5–9), Journeyman (10–19), Adept (20–29), Expert (30–39), Master (40–49), Grandmaster (50+)
- Activity cooldowns validated server-side via `ActivityLog` timestamp queries
- Feed queries join with `Friendship` table for friends-only feed
- 254 unit/controller tests pass, 0 failures. Integration tests (Testcontainer) need Docker runtime.
- `ProfileCardResponse` extended with backward-compatible `fromUser()` overload
