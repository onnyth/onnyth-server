# S5-03 — User Position: Service + Endpoint

> **Status**: `NOT_STARTED`
> **Priority**: 🔴 Must
> **Depends on**: S5-02

## Goal

Implement `GET /api/v1/leaderboard/my-position` — returns the current user's position among friends, points gap to the next position, and the user ahead.

## Tasks

### Service
- [ ] Implement `getUserPosition(UUID userId)` in `LeaderboardService`:
  1. Get friend IDs + self
  2. Sort all by totalScore DESC
  3. Find current user's position
  4. Find user directly ahead (position - 1)
  5. Compute `pointsToNextPosition` = score of user ahead - my score
  6. Return `UserLeaderboardPositionResponse`

### Controller
- [ ] `GET /api/v1/leaderboard/my-position` endpoint on `LeaderboardController`

### Tests
- [ ] Unit tests for `getUserPosition()` — various scenarios (first place, last place, tied)
- [ ] Controller test for the endpoint

## Related Skills
- `conventions` — service/controller patterns
