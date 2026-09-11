# S7-06 — Profile Card Extension

> **Status**: `NOT_STARTED`
> **Priority**: 🔴 Must
> **Blocks**: S-037

## Goal

Extend the existing `GET /api/v1/profile/card` response to include level, title, and current streak data.

## Tasks

### ProfileCardResponse Changes
- [ ] Add `level` (int) field
- [ ] Add `levelTitle` (String) field
- [ ] Add `currentStreak` (int) field
- [ ] Update `fromUser()` factory to populate new fields

### ProfileService Changes
- [ ] Inject `LevelService` and `StreakService` (or `UserStreakRepository`)
- [ ] Populate level/title from `User.level` + `LevelService.getTitle()`
- [ ] Populate streak from `UserStreak` entity

### Profile Card for Friends
- [ ] Ensure `GET /api/v1/users/{id}/card` (public card) also includes the new fields

## Related Skills
- `user-profile` — profile card pattern
- `conventions` — DTO pattern
