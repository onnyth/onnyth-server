# S7-05 — Streak System: StreakService + GET /streaks

> **Status**: `NOT_STARTED`
> **Priority**: 🔴 Must
> **Blocks**: S-040

## Goal

Track daily activity streaks. Increment on consecutive daily activity, reset on missed days. Publish events at milestones.

## Tasks

### StreakService
- [ ] Create `StreakService` with:
  - `recordActivity(UUID userId)`:
    1. Get or create `UserStreak` for user
    2. If `lastActivityDate == today` → do nothing (already counted)
    3. If `lastActivityDate == yesterday` → `currentStreak++`
    4. If `lastActivityDate < yesterday` (or null) → `currentStreak = 1` (broken/new)
    5. Update `longestStreak = max(currentStreak, longestStreak)`
    6. Update `lastActivityDate = today`
    7. If `currentStreak` in [7, 14, 30, 50, 100] → publish milestone event
  - `getStreak(UUID userId)` — returns `StreakResponse`

### Streak Milestone Event
- [ ] Publish a `FeedEvent` with type `STREAK` when user hits milestone streak days

### Controller Endpoint
- [ ] `GET /api/v1/streaks` — returns `StreakResponse` for authenticated user
- [ ] Add to `ActivityController` or create `StreakController`

### StreakResponse.isActive
- [ ] `isActive = lastActivityDate == today OR lastActivityDate == yesterday`

## Related Skills
- `conventions` — service pattern
