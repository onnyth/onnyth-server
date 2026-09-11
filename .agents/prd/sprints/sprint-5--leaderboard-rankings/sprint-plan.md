# Sprint Plan — Sprint 5: Leaderboard & Rankings

> **Goal**: Build friends leaderboard, user position tracking, category filters, and weekly snapshot system
> **Status**: `COMPLETED`
> **Source**: [Backend Brief](backend-brief.md)
> **Client Sprint**: Sprint 3
> **Source PRD**: PRD-003 — Leaderboard & Rankings

## Context

Client Sprint 3 is building the Leaderboard & Rankings UI with **full mock mode** — no backend endpoints are blocking client work. Backend should prioritize core leaderboard + position (orders 1-4) to enable real API integration.

> [!IMPORTANT]
> Brief specifies V8 migration, but V7+V8 already exist (friends system, Sprint 4). Using **V9** instead.

> [!IMPORTANT]
> `FriendshipRepository.findFriendIdsByUserId()` did not exist — added in S5-01.

## Sprint Scope

| # | Story | Client Stories | Priority | Status |
|---|---|---|---|---|
| S5-01 | Data Model — LeaderboardSnapshot + DTOs + Repo additions | All leaderboard features | 🔴 Must | ✅ |
| S5-02 | Friends Leaderboard — Service + Endpoint | S-018, S-023 | 🔴 Must | ✅ |
| S5-03 | User Position — Service + Endpoint | S-020 | 🔴 Must | ✅ |
| S5-04 | Category Leaderboard | S-021 | 🔴 Must | ✅ |
| S5-05 | Weekly Snapshot + Rank Changes | S-022 | 🟡 Should | ✅ |

## Stories

- [x] [S5-01 — Data Model](stories/S5-01--data-model.md)
- [x] [S5-02 — Friends Leaderboard](stories/S5-02--friends-leaderboard.md)
- [x] [S5-03 — User Position](stories/S5-03--user-position.md)
- [x] [S5-04 — Category Leaderboard](stories/S5-04--category-leaderboard.md)
- [x] [S5-05 — Weekly Snapshot](stories/S5-05--weekly-snapshot.md)

## Ordering Rationale

1. **S5-01 (Data Model)** first — entity, migration, DTOs, repository additions are foundation
2. **S5-02 (Friends Leaderboard)** second — core `GET /leaderboard` endpoint
3. **S5-03 (User Position)** third — `GET /leaderboard/my-position` endpoint
4. **S5-04 (Category Leaderboard)** fourth — extends leaderboard with `?category=` filter
5. **S5-05 (Weekly Snapshot)** last — lower priority scheduled job + position change tracking

## Sprint Notes

- Migration numbered V9 (V7+V8 were taken by Sprint 4 friends system).
- `FriendshipRepository.findFriendIdsByUserId()` added — was not present from Sprint 4.
- `@EnableScheduling` added to `OnnythServerApplication` for the weekly snapshot `@Scheduled` job.
- Fixed immutable list sorting issue (`new ArrayList<>()` wrapper on `findAllById()` results).
- 9 new tests added (5 service + 4 controller). All pass.
