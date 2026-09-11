# S5-05 — Weekly Snapshot + Rank Changes

> **Status**: `NOT_STARTED`
> **Priority**: 🟡 Should
> **Depends on**: S5-02

## Goal

Implement the weekly snapshot system that records leaderboard positions every Sunday and enables `positionChange` / `isNew` flags in leaderboard responses.

## Tasks

### Application Config
- [ ] Add `@EnableScheduling` to `OnnythServerApplication`

### Service
- [ ] Create `LeaderboardSnapshotService`
- [ ] Implement `takeWeeklySnapshot()` with `@Scheduled(cron = "0 0 0 * * SUN")`:
  1. For each user, compute their position among their friends
  2. Save `LeaderboardSnapshot` rows with current positions
  3. Log completion
- [ ] Implement `getPositionChanges(UUID friendOwnerId, List<UUID> userIds)`:
  1. Find last Sunday's snapshot for this friend group
  2. Compare current positions vs snapshot positions
  3. Return map of userId → positionChange (positive = moved up, negative = moved down)
  4. Users not in snapshot are marked `isNew = true`

### Integration
- [ ] Update `LeaderboardService.getFriendsLeaderboard()` to call snapshot service for `positionChange` and `isNew` fields
- [ ] Update `LeaderboardService.getLeaderboardByCategory()` similarly

### Tests
- [ ] Unit tests for `LeaderboardSnapshotService`
- [ ] Unit tests for position change calculation
- [ ] Integration test for the scheduled job (optional — may defer)

## Related Skills
- `data-layer` — LeaderboardSnapshot entity
- `conventions` — service patterns
