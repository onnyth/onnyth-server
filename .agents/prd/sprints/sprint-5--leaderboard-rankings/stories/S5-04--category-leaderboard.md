# S5-04 — Category Leaderboard

> **Status**: `NOT_STARTED`
> **Priority**: 🔴 Must
> **Depends on**: S5-02

## Goal

Extend `GET /api/v1/leaderboard` with optional `?category=` parameter to filter leaderboard by a specific stat category (e.g., `CAREER`, `FITNESS`). Uses per-stat value instead of total score.

## Tasks

### Service
- [ ] Implement `getLeaderboardByCategory(UUID userId, StatCategory category, Pageable pageable)` in `LeaderboardService`:
  1. Get friend IDs + self
  2. Fetch `LifeStat` for all users for the given category
  3. Sort by stat value DESC; users without that stat ranked last (value = 0)
  4. Build `CategoryLeaderboardEntryResponse` list with positions
  5. Mark `isCurrentUser`

### Controller
- [ ] Add optional `@RequestParam StatCategory category` to `GET /api/v1/leaderboard`
- [ ] When `category` is present, call `getLeaderboardByCategory()`; otherwise call `getFriendsLeaderboard()`

### Tests
- [ ] Unit tests for category leaderboard with mixed stat presence
- [ ] Controller test with `?category=CAREER`

## Related Skills
- `data-layer` — LifeStatRepository query
- `conventions` — controller patterns
