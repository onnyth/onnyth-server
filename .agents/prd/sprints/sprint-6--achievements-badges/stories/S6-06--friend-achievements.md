# S6-06 — Friend Achievements

> **Status**: `NOT_STARTED`
> **Priority**: 🟡 Medium
> **Depends on**: S6-02

## Goal

Implement endpoints to view a friend's achievements and stats.

## Tasks

### Service
- [ ] `getFriendAchievements(UUID userId, UUID friendId)` → List<AchievementResponse>
  - Validate friendship exists
  - Return friend's unlocked achievements
- [ ] `getFriendAchievementStats(UUID userId, UUID friendId)` → AchievementStatsResponse

### Controller
- [ ] `GET /api/v1/achievements/user/{userId}` — friend's achievements
- [ ] `GET /api/v1/achievements/user/{userId}/stats` — friend's achievement stats

### Tests
- [ ] Unit tests for friend achievement access
- [ ] Controller test including friendship validation

## Related Skills
- `conventions` — controller patterns
