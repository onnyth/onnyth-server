# S6-03 — Achievement Stats + Progress

> **Status**: `NOT_STARTED`
> **Priority**: 🔴 Must
> **Depends on**: S6-02

## Goal

Implement `GET /achievements/stats` endpoint and `AchievementProgressCalculator` for computing 0–100 progress per achievement.

## Tasks

### Progress Calculator
- [ ] Create `AchievementProgressCalculator`
- [ ] `calculateProgress(UUID userId, Achievement achievement)` → int (0–100)
- [ ] Support requirement types: stat value thresholds, friend count, total score, streak

### Service
- [ ] `getAchievementStats(UUID userId)` → AchievementStatsResponse

### Controller
- [ ] `GET /api/v1/achievements/stats` endpoint

### Tests
- [ ] Unit tests for progress calculator
- [ ] Unit test for stats method

## Related Skills
- `conventions` — service patterns
