## 5. Backend Sprint Brief

> **Source PRD**: PRD-005
> **Client Sprints**: Sprint 6, 7, 8
> **Date**: 2026-03-27

---

### 5.1. API Endpoints Required

| Client Story | Method | Endpoint | Request Body | Response Body | Notes |
|---|---|---|---|---|---|
| S-031 | GET | `/api/v1/activities/types` | — | `ActivityType[]` | List all activity types, optionally filter by category query param |
| S-033 | POST | `/api/v1/activities/log` | `{ activityTypeId: string }` | `ActivityLogResponse { id, activityType, xpEarned, loggedAt, newTotalXP, newLevel?, levelTitle?, streakUpdated }` | Core logging endpoint. Awards XP, checks level-up, updates streak |
| S-033 | GET | `/api/v1/activities/history` | Query: `page, size` | `Page<ActivityLogResponse>` | User's activity history |
| S-033 | GET | `/api/v1/activities/status` | — | `{ todayLogs: ActivityType[], cooldowns: { activityTypeId, availableAt }[] }` | What user already logged today + cooldowns |
| S-036 | GET | `/api/v1/profile/level` | — | `LevelProgressResponse { currentLevel, title, currentXP, xpForNextLevel, progressPercent }` | Or include in profile card endpoint |
| S-040 | GET | `/api/v1/streaks` | — | `StreakResponse { currentStreak, longestStreak, lastActivityDate, isActive }` | Current user's streak data |
| S-043 | GET | `/api/v1/feed` | Query: `page, size` | `Page<FeedEvent>` | Friends-only activity feed, 4 event types |
| S-047 | GET | `/api/v1/store/items` | Query: `category?` | `CosmeticItem[]` | List store items |
| S-049 | POST | `/api/v1/store/purchase` | `{ itemId: string }` | `{ success, item: CosmeticItem }` | Purchase an item |
| S-049 | PUT | `/api/v1/store/equip` | `{ itemId: string }` | `{ equippedItems: CosmeticItem[] }` | Equip a cosmetic |
| S-049 | GET | `/api/v1/store/inventory` | — | `CosmeticItem[]` | User's owned cosmetics |
| S-037 | GET | `/api/v1/profile/card` | — | Extended: add `level`, `title`, `currentStreak` | MODIFY existing endpoint |

---

### 5.2. Data Model Changes

| Entity | Change Type | Details |
|---|---|---|
| `ActivityType` | NEW | id (UUID), name, description, icon, category (StatCategory enum), xpReward (int), frequency ('DAILY' / 'WEEKLY'), cooldownHours (int), isActive (bool) |
| `ActivityLog` | NEW | id (UUID), userId (FK→User), activityTypeId (FK→ActivityType), xpEarned (int), loggedAt (timestamp). Index on (userId, activityTypeId, loggedAt) for cooldown checks |
| `User` | MODIFY | Add `xp` (bigint, default 0), `level` (int, default 1) columns |
| `UserStreak` | NEW | userId (FK→User, unique), currentStreak (int), longestStreak (int), lastActivityDate (date) |
| `FeedEvent` | NEW | id (UUID), userId (FK), eventType enum ('ACTIVITY', 'LEVEL_UP', 'ACHIEVEMENT', 'STREAK'), eventData (JSONB), createdAt (timestamp). Index on (createdAt) + friend-join query |
| `CosmeticItem` | NEW | id (UUID), name, description, previewUrl, category (CosmeticCategory enum), price (int), rarity ('COMMON', 'RARE', 'EPIC', 'LEGENDARY'), isActive (bool) |
| `UserCosmetic` | NEW | userId (FK), cosmeticItemId (FK), purchasedAt (timestamp), isEquipped (bool). Unique on (userId, cosmeticItemId) |
| `CosmeticCategory` | NEW ENUM | PROFILE_THEME, TITLE, BADGE_FRAME, AVATAR_SKIN, GLOW_EFFECT, BANNER |
| `ActivityFrequency` | NEW ENUM | DAILY, WEEKLY |
| `FeedEventType` | NEW ENUM | ACTIVITY, LEVEL_UP, ACHIEVEMENT, STREAK |

---

### 5.3. Business Logic / Services

| Service | Purpose | Triggered By |
|---|---|---|
| `ActivityService` | Validate activity log (cooldown check, frequency check), persist log, award XP, publish events | Client story S-033 |
| `XpService` | Award XP to user, update totalXP, check for level-up, publish LevelUpEvent | ActivityService after log |
| `LevelService` | Calculate level from XP (progressive curve), determine title, return level progress | XpService, Profile endpoint |
| `StreakService` | Check lastActivityDate, increment/reset streak, check milestones, publish streak events | ActivityService after log |
| `FeedService` | Create feed events, query friend feed with pagination | Multiple event sources |
| `CosmeticService` | List store items, process purchase, equip/unequip, validate ownership | Client store stories S-047–S-050 |

**Level XP Curve** (progressive, levels get harder):
```
Level 1→2:   100 XP
Level 2→3:   100 XP
Level 3→4:   100 XP
Level 5→6:   150 XP
Level 10→11: 200 XP
Level 20→21: 300 XP
Level 50→51: 500 XP
...
Formula suggestion: xpForLevel(n) = 100 + floor((n-1) / 5) * 50
```

**Streak Logic**:
```
On activity log:
  1. Get UserStreak for user
  2. If lastActivityDate == today → do nothing (already counted)
  3. If lastActivityDate == yesterday → currentStreak++
  4. If lastActivityDate < yesterday → currentStreak = 1 (broken)
  5. Update longestStreak = max(currentStreak, longestStreak)
  6. Update lastActivityDate = today
  7. If currentStreak in [7, 14, 30, 50, 100] → create STREAK feed event
```

---

### 5.4. Client → Backend Story Mapping

| Client Story | Backend Work Required | Backend Priority |
|---|---|---|
| S-031 | ActivityType entity, migration, repo, seed data | 🔴 Must (blocks client) |
| S-032 | ActivityType seed migration with 25+ activities | 🔴 Must (blocks client) |
| S-033 | POST /log endpoint + ActivityLog entity + cooldown validation | 🔴 Must (blocks client) |
| S-034 | None (pure frontend animation) | — |
| S-035 | None (pure frontend navigation) | — |
| S-036 | xp/level columns on User + LevelService + GET /level endpoint | 🔴 Must (blocks client) |
| S-037 | Extend GET /profile/card with level, title, streak fields | 🔴 Must (blocks client) |
| S-038 | None (pure frontend, uses S-033 response data) | — |
| S-039 | GET /profile/level endpoint (may share with S-036) | 🟡 Should |
| S-040 | UserStreak entity + StreakService + GET /streaks endpoint | 🔴 Must (blocks client) |
| S-041 | None (pure frontend, uses S-040 data) | — |
| S-042 | None (uses existing profile + streak data) | — |
| S-043 | FeedEvent entity + FeedService + GET /feed endpoint | 🔴 Must (blocks client) |
| S-044 | Feed event creation on activity log, level-up, achievement unlock | 🔴 Must (blocks client) |
| S-045 | None (uses S-043 endpoint with page param) | — |
| S-046 | None (uses S-043 endpoint pagination) | — |
| S-047 | CosmeticItem entity + seed data + GET /store/items | 🟡 Should |
| S-048 | None (pure frontend, uses S-047 data) | — |
| S-049 | POST /purchase + PUT /equip + GET /inventory + UserCosmetic entity | 🟡 Should |
| S-050 | None (pure frontend navigation) | — |

---

### 5.5. Suggested Backend Sprint Ordering

| Order | Backend Task | Blocks Client Story | Notes |
|---|---|---|---|
| 1 | ActivityType entity + repo + migration + seed data (25+ activities) | S-031, S-032 | Foundation — must be done before Sprint 6 starts |
| 2 | ActivityLog entity + repo + migration | S-033 | Core data model for logging |
| 3 | User entity: add xp, level columns + migration | S-036, S-037 | Extend existing entity |
| 4 | ActivityService (log + cooldown validation + XP award) | S-033 | Core business logic |
| 5 | XpService + LevelService (level calculation, title mapping) | S-036, S-037 | Level progression engine |
| 6 | POST /activities/log + GET /activities/types + GET /activities/status | S-033 | Core API endpoints |
| 7 | Extend GET /profile/card with level, title, streak | S-037 | Modify existing endpoint |
| 8 | GET /profile/level endpoint | S-039 | Level progress detail |
| 9 | UserStreak entity + StreakService + migration | S-040 | Can start in parallel with Order 4-6 |
| 10 | GET /streaks endpoint | S-040 | Streak API |
| 11 | FeedEvent entity + FeedService + migration | S-043 | Feed data layer |
| 12 | Feed event creation in ActivityService, LevelService, AchievementService, StreakService | S-044 | Integration points |
| 13 | GET /feed endpoint with friend-join and pagination | S-043 | Feed API |
| 14 | CosmeticItem + UserCosmetic entities + migrations + seed data | S-047 | Cosmetic data model |
| 15 | CosmeticService + store endpoints (GET items, POST purchase, PUT equip, GET inventory) | S-049 | Cosmetic APIs |

---

### 5.6. Notes for Backend Agent

- **Existing patterns to follow**: The existing `Quest` model, `AchievementService`, and `StatChangedEvent` pattern provide reference for entity design, service structure, and event publishing.
- **The existing quest system** can coexist with activities. Quests = system-created challenges, Activities = user-logged daily actions. No need to merge or remove quests.
- **Activity cooldown validation** is critical: check `ActivityLog` for existing entries within the cooldown period before allowing a new log.
- **Feed event query** should join with the user's friend list (from existing `FriendShip` entity) and order by `createdAt DESC` with pagination.
- **Level XP curve** should be server-computed so it can be tuned without client updates.
- **All endpoints use JWT auth** — follow existing `@PreAuthorize` patterns.
