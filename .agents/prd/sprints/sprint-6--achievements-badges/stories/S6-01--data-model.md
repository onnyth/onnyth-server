# S6-01 — Data Model: Achievement + UserAchievement + Migrations + DTOs + Repos

> **Status**: `NOT_STARTED`
> **Priority**: 🔴 Must

## Goal

Create all foundational components: entities, enum, migrations (V10–V13), DTOs, and repositories.

## Tasks

### Enum
- [ ] Create `AchievementCategory` enum: `STATS`, `SOCIAL`, `STREAK`, `MILESTONE`, `SPECIAL`

### Entities
- [ ] Create `Achievement` entity (achievements table): id (UUID), code (unique), name, description, icon, category (AchievementCategory), requirementType (String), threshold (int), points (int), isActive (boolean)
- [ ] Create `UserAchievement` entity (user_achievements table): id (UUID), userId, achievementId, unlockedAt (Instant)

### Migrations
- [ ] `V10__create_achievements_table.sql` — achievements table with unique constraint on code
- [ ] `V11__create_user_achievements_table.sql` — user_achievements join table with unique(userId, achievementId)
- [ ] `V12__add_displayed_achievements_to_users.sql` — user_displayed_achievements @ElementCollection table
- [ ] `V13__seed_initial_achievements.sql` — INSERT 12 default achievements across 5 categories

### DTOs
- [ ] `AchievementResponse` record: id, name, description, icon, category, points, isUnlocked, progress, unlockedAt
- [ ] `AchievementStatsResponse` record: totalAchievements, unlockedCount, totalPoints, earnedPoints
- [ ] `DisplayedBadgeRequest` record: achievementIds (List<UUID>, max 3)
- [ ] `DisplayedBadgeResponse` record: id, name, icon, achievementId
- [ ] `AchievementUnlockResponse` record: unlockedAchievements (List<AchievementResponse>)

### User Entity Change
- [ ] Add `displayedAchievements` field — `@ElementCollection` List<UUID> (max 3)

### Repositories
- [ ] `AchievementRepository`: findAll, findByCategory, findByCode
- [ ] `UserAchievementRepository`: findAllByUserId, findByUserIdAndAchievementId, countByUserId, existsByUserIdAndAchievementId

## Related Skills
- `data-layer` — entity/migration patterns
- `conventions` — DTO pattern
