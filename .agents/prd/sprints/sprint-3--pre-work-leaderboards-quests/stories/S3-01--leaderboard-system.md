# S3-01 — Leaderboard System

> **Status**: `DONE`
> **Priority**: 🔴 Must
> **Sprint**: Sprint 3
> **Blocks Client Stories**: S-006 (Leaderboards placeholder → future real tab)

## What & Why

Build the backend leaderboard system so the client can switch from "Coming Soon" placeholders to real leaderboard data in a future sprint. Users should be able to see global rankings by total score.

## API Endpoints (from Brief §1)

| Method | Endpoint | Request Body | Response Body | Notes |
|---|---|---|---|---|
| GET | `/api/v1/leaderboards` | `?type=global&limit=50&offset=0` | `LeaderboardResponse` | Global leaderboard sorted by total score |

## Technical Tasks

### Data Model (from Brief §2)
- [ ] No new table needed — leaderboard is a **query on the existing `users` table** (sorted by `total_score` DESC)
- [ ] Consider whether a materialized view is needed (skip for now — direct query is fine at this scale)

### Services (from Brief §3)
- [ ] Create `LeaderboardService.java` — fetch ranked users with pagination
- [ ] Add method: `getGlobalLeaderboard(int limit, int offset)` → queries users ordered by `total_score DESC`
- [ ] Include the requesting user's own rank position in the response

### Controllers & DTOs
- [ ] Create `LeaderboardEntryResponse.java` — `rank`, `userId`, `username`, `fullName`, `profilePic`, `totalScore`, `rankTier`
- [ ] Create `LeaderboardResponse.java` — `entries: List<LeaderboardEntryResponse>`, `userRank` (nullable), `totalUsers`
- [ ] Create `LeaderboardController.java` — `GET /api/v1/leaderboards` with query params `type`, `limit`, `offset`

### Tests
- [ ] Unit test: `LeaderboardServiceTest.java`
- [ ] Controller test: `LeaderboardControllerTest.java`

## Related Skills

- `conventions` — coding patterns (DTOs, services, controllers)
- `data-layer` — User entity (querying `total_score`)
- `api-reference` — existing endpoints (avoid path conflicts)
- `testing` — test structure and patterns

## Skill Updates Required

- [ ] `api-reference/SKILL.md` — Added `GET /api/v1/leaderboards` endpoint
- [ ] `data-layer/SKILL.md` — No entity change, but note the leaderboard query pattern
- [ ] `testing/SKILL.md` — Added LeaderboardServiceTest, LeaderboardControllerTest

## Notes

- Start with a simple `ORDER BY total_score DESC` query — no caching or materialized views needed yet
- `type` parameter defaults to `global` (only supported type for now, extensible to `friends` later)
- `limit` defaults to 50, max 100. `offset` defaults to 0 for pagination
- User's own rank is computed separately to show "You are ranked #X" regardless of pagination
