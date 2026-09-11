# S6-04 — Achievement Unlock Engine

> **Status**: `NOT_STARTED`
> **Priority**: 🔴 Must
> **Depends on**: S6-03

## Goal

Implement `AchievementUnlockService` that evaluates and unlocks achievements, plus integrate into existing flows.

## Tasks

### Service
- [ ] Create `AchievementUnlockService`
- [ ] `checkAndUnlockAchievements(UUID userId)` → List<Achievement> (newly unlocked)
  - Get all achievements not yet unlocked by user
  - Evaluate each using `AchievementProgressCalculator`
  - Unlock (save `UserAchievement`) where progress == 100
  - Return list of newly unlocked

### Integration
- [ ] Call `checkAndUnlockAchievements()` after `ScoreCalculationService.recalculateUserScore()` (via event listener on StatChangedEvent)
- [ ] Call `checkAndUnlockAchievements()` after `FriendshipService.acceptFriendRequest()`

### Tests
- [ ] Unit tests for unlock service
- [ ] Verify integration hook wiring

## Related Skills
- `conventions` — event-driven patterns
- `scoring-and-ranking` — score recalculation flow
