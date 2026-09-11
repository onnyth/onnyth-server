# S7-01 — Data Model: Entities, Enums, Migrations, DTOs, Repos

> **Status**: `NOT_STARTED`
> **Priority**: 🔴 Must

## Goal

Create all foundational data components for the Activity & Progression system: entities, enums, Flyway migrations, DTOs, and repositories.

## Tasks

### Enums
- [ ] Create `ActivityFrequency` enum: `DAILY`, `WEEKLY`
- [ ] Create `FeedEventType` enum: `ACTIVITY`, `LEVEL_UP`, `ACHIEVEMENT`, `STREAK`
- [ ] Create `CosmeticCategory` enum: `PROFILE_THEME`, `TITLE`, `BADGE_FRAME`, `AVATAR_SKIN`, `GLOW_EFFECT`, `BANNER`
- [ ] Create `CosmeticRarity` enum: `COMMON`, `RARE`, `EPIC`, `LEGENDARY`

### Entities
- [ ] `ActivityType` — id (UUID), name, description, icon, category (StatCategory), xpReward (int), frequency (ActivityFrequency), cooldownHours (int), isActive (boolean)
- [ ] `ActivityLog` — id (UUID), userId (FK→User), activityTypeId (FK→ActivityType), xpEarned (int), loggedAt (Instant)
- [ ] `UserStreak` — id (UUID), userId (FK→User, unique), currentStreak (int), longestStreak (int), lastActivityDate (LocalDate)
- [ ] `FeedEvent` — id (UUID), userId (FK→User), eventType (FeedEventType), eventData (JSONB/TEXT), createdAt (Instant)
- [ ] `CosmeticItem` — id (UUID), name, description, previewUrl, category (CosmeticCategory), price (int), rarity (CosmeticRarity), isActive (boolean)
- [ ] `UserCosmetic` — id (UUID), userId (FK→User), cosmeticItemId (FK→CosmeticItem), purchasedAt (Instant), isEquipped (boolean). Unique on (userId, cosmeticItemId)

### User Entity Changes
- [ ] Add `xp` field (Long, default 0) to `User`
- [ ] Add `level` field (Integer, default 1) to `User`

### Migrations
- [ ] `V14__create_activity_types_table.sql` — activity_types table
- [ ] `V15__create_activity_log_table.sql` — activity_log with index on (user_id, activity_type_id, logged_at)
- [ ] `V16__add_xp_level_to_users.sql` — Add xp (BIGINT DEFAULT 0) and level (INT DEFAULT 1) to users
- [ ] `V17__create_user_streaks_table.sql` — user_streaks with unique constraint on user_id
- [ ] `V18__create_feed_events_table.sql` — feed_events with index on created_at
- [ ] `V19__create_cosmetic_tables.sql` — cosmetic_items + user_cosmetics tables
- [ ] `V20__seed_activity_types.sql` — INSERT 25+ activity types across 5 stat categories

### DTOs
- [ ] `ActivityTypeResponse` record: id, name, description, icon, category, xpReward, frequency, cooldownHours
- [ ] `ActivityLogResponse` record: id, activityType (ActivityTypeResponse), xpEarned, loggedAt, newTotalXP, newLevel, levelTitle, streakUpdated
- [ ] `ActivityStatusResponse` record: todayLogs (List), cooldowns (List of cooldown entries)
- [ ] `LevelProgressResponse` record: currentLevel, title, currentXP, xpForNextLevel, progressPercent
- [ ] `StreakResponse` record: currentStreak, longestStreak, lastActivityDate, isActive
- [ ] `FeedEventResponse` record: id, userId, username, profilePic, eventType, eventData, createdAt
- [ ] `CosmeticItemResponse` record: id, name, description, previewUrl, category, price, rarity, isOwned, isEquipped
- [ ] `LogActivityRequest` record: activityTypeId (UUID, @NotNull)
- [ ] `PurchaseRequest` record: itemId (UUID, @NotNull)
- [ ] `EquipRequest` record: itemId (UUID, @NotNull)

### Repositories
- [ ] `ActivityTypeRepository`: findAll, findAllByCategory, findByIdAndIsActiveTrue
- [ ] `ActivityLogRepository`: findAllByUserIdOrderByLoggedAtDesc (Pageable), findAllByUserIdAndLoggedAtBetween, findByUserIdAndActivityTypeIdAndLoggedAtAfter
- [ ] `UserStreakRepository`: findByUserId
- [ ] `FeedEventRepository`: findFriendFeed (custom query joining friendships, Pageable)
- [ ] `CosmeticItemRepository`: findAllByIsActiveTrue, findAllByIsActiveTrueAndCategory
- [ ] `UserCosmeticRepository`: findAllByUserId, findByUserIdAndCosmeticItemId, existsByUserIdAndCosmeticItemId

## Related Skills
- `data-layer` — entity/migration patterns
- `conventions` — DTO pattern, entity pattern
