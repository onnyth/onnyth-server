# S6-05 — Badge Display Slots

> **Status**: `NOT_STARTED`
> **Priority**: 🔴 Must
> **Depends on**: S6-01

## Goal

Implement badge display slots — users can showcase up to 3 unlocked achievements on their profile.

## Tasks

### Service
- [ ] `getDisplayedBadges(UUID userId)` → List<DisplayedBadgeResponse>
- [ ] `updateDisplayedBadges(UUID userId, List<UUID> achievementIds)` — validate: all unlocked, max 3, no duplicates

### Controller
- [ ] `GET /api/v1/achievements/displayed`
- [ ] `PUT /api/v1/achievements/displayed` with `DisplayedBadgeRequest` body

### Exceptions
- [ ] `BadgeNotFoundException` — invalid achievement ID (404)
- [ ] `BadgeNotUnlockedException` — trying to display a locked achievement (400)

### Tests
- [ ] Unit tests for display badge logic
- [ ] Controller tests

## Related Skills
- `conventions` — controller patterns
- `error-handling` — custom exceptions
