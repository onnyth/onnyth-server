# Sprint Plan — Sprint 6: Achievements & Badges

> **Goal**: Build achievement system with unlock tracking, progress calculation, badge display slots, and friend achievement viewing
> **Status**: `COMPLETED`
> **Source**: [Backend Brief](backend-brief.md)
> **Client Sprint**: Sprint 4
> **Source PRD**: PRD-004 — Achievements & Badges

## Context

Client Sprint 4 is building Achievements & Badges UI with **full mock mode** — no backend endpoints blocking client work. Backend should prioritize data model + catalog + unlock service to enable real API integration.

> [!IMPORTANT]
> Brief specifies V9–V12 migrations, but V9 is taken (leaderboard snapshots, Sprint 5). Using **V10–V13** instead.

## Sprint Scope

| # | Story | Client Stories | Priority | Status |
|---|---|---|---|---|
| S6-01 | Data Model — Achievement + UserAchievement entities, migrations, DTOs, repos | All | 🔴 Must | ✅ |
| S6-02 | Achievement Catalog — Service + Endpoints | S-025 | 🔴 Must | ✅ |
| S6-03 | Achievement Stats + Progress | S-026 | 🔴 Must | ✅ |
| S6-04 | Achievement Unlock Engine | S-027 | 🔴 Must | ✅ |
| S6-05 | Badge Display Slots | S-028 | 🔴 Must | ✅ |
| S6-06 | Friend Achievements | S-030 | 🟡 Medium | ✅ |

## Stories

- [x] [S6-01 — Data Model](stories/S6-01--data-model.md)
- [x] [S6-02 — Achievement Catalog](stories/S6-02--achievement-catalog.md)
- [x] [S6-03 — Achievement Stats](stories/S6-03--achievement-stats.md)
- [x] [S6-04 — Achievement Unlock Engine](stories/S6-04--unlock-engine.md)
- [x] [S6-05 — Badge Display Slots](stories/S6-05--badge-display.md)
- [x] [S6-06 — Friend Achievements](stories/S6-06--friend-achievements.md)

## Ordering Rationale

1. **S6-01 (Data Model)** first — entity, enum, migrations, DTOs, repos are foundation
2. **S6-02 (Catalog)** second — core `GET /achievements` endpoints
3. **S6-03 (Stats)** third — `GET /achievements/stats` + progress calculation
4. **S6-04 (Unlock Engine)** fourth — `AchievementUnlockService` + integration hooks
5. **S6-05 (Badge Display)** fifth — `displayedAchievements` on User + display endpoints
6. **S6-06 (Friend Achievements)** last — lower priority friend-facing endpoints

## Sprint Notes

- Migrations V10–V13 (V9 was taken by Sprint 5 leaderboard)
- `ApiException` uses single-arg constructor + abstract `getHttpStatus()` — fixed `BadgeNotFoundException`/`BadgeNotUnlockedException`.
- `displayedAchievements` on User uses `@ElementCollection` with separate collection table.
- Integration hooks: `ScoreCalculationService.recalculateUserScore()` and `FriendshipService.acceptFriendRequest()` now trigger `checkAndUnlockAchievements()`.
- 26 tests pass (7 new achievement tests + 19 pre-existing).
- `AchievementProgressCalculator` supports 8 requirement types including streak placeholder.
