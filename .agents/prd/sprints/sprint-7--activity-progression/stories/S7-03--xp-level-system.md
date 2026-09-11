# S7-03 — XP & Level System

> **Status**: `NOT_STARTED`
> **Priority**: 🔴 Must
> **Blocks**: S-036

## Goal

Create XpService and LevelService to handle XP awarding and level progression. Level titles are strings mapped to level milestones.

## Tasks

### XpService
- [ ] Create `XpService` with:
  - `awardXp(UUID userId, int amount)` — add XP to user, trigger level check
  - Returns new total XP
- [ ] Publish `XpAwardedEvent` (record in `events/` package): userId, xpAmount, newTotalXp

### LevelService
- [ ] Create `LevelService` with:
  - `calculateLevel(long totalXp)` — compute level from XP using progressive curve
  - `xpForLevel(int level)` — XP required to reach a given level: `100 + floor((level-1) / 5) * 50`
  - `xpForNextLevel(int currentLevel)` — XP needed for next level
  - `getTitle(int level)` — map level to a title string (e.g. "Novice", "Apprentice", "Journeyman", etc.)
  - `getLevelProgress(UUID userId)` — returns `LevelProgressResponse`
  - `checkAndUpdateLevel(UUID userId)` — check if user should level up, update `level` field, publish `LevelUpEvent` if changed

### LevelUpEvent
- [ ] Create `LevelUpEvent` record in `events/`: userId, oldLevel, newLevel, newTitle

### Level Endpoint
- [ ] `GET /api/v1/profile/level` — returns `LevelProgressResponse`
- [ ] Add to `ProfileController` or create new endpoint in `ActivityController`

### Level Titles (suggested progression)
- [ ] Define title mapping:
  - Level 1–4: "Novice"
  - Level 5–9: "Apprentice"
  - Level 10–19: "Journeyman"
  - Level 20–29: "Adept"
  - Level 30–39: "Expert"
  - Level 40–49: "Master"
  - Level 50+: "Grandmaster"

## Related Skills
- `scoring-and-ranking` — similar event-driven recalc pattern
- `conventions` — event, service patterns
