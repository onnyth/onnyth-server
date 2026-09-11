# Backend Sprint Brief — Sprint 4: Achievements & Badges

> **Sprint**: Sprint 4
> **Source PRD**: PRD-004
> **Date**: 2026-03-04

---

## 1. API Endpoints Required

| Method | Endpoint | Client Story | Priority |
|---|---|---|---|
| GET | `/api/v1/achievements` | S-025 | 🔴 Blocking |
| GET | `/api/v1/achievements/category/{category}` | S-025 | 🟡 Nice-to-have (client filters locally) |
| GET | `/api/v1/achievements/unlocked` | S-025 | 🔴 Blocking |
| GET | `/api/v1/achievements/stats` | S-026 | 🔴 Blocking |
| PUT | `/api/v1/achievements/displayed` | S-028 | 🔴 Blocking |
| GET | `/api/v1/achievements/displayed` | S-028 | 🔴 Blocking |
| GET | `/api/v1/achievements/user/{userId}` | S-030 | 🟡 Medium |
| GET | `/api/v1/achievements/user/{userId}/stats` | S-030 | 🟡 Medium |

---

## 2. Data Model Changes

### New Entities

| Entity | Fields | Notes |
|---|---|---|
| `Achievement` | id (UUID), code (String, unique), name, description, icon, category (AchievementCategory enum), requirementType, threshold, points, isActive | Master achievement definitions, seeded via migration |
| `AchievementCategory` | `STATS`, `SOCIAL`, `STREAK`, `MILESTONE`, `SPECIAL` | Enum |
| `UserAchievement` | id (UUID), userId, achievementId, unlockedAt (Instant) | Tracks per-user unlock status |

### New DTOs

| DTO | Fields |
|---|---|
| `AchievementResponse` | id, name, description, icon, category, points, isUnlocked, progress (0–100), unlockedAt |
| `AchievementStatsResponse` | totalAchievements, unlockedCount, totalPoints, earnedPoints |
| `DisplayedBadgeRequest` | achievementIds (list of up to 3 UUIDs) |
| `DisplayedBadgeResponse` | id, name, icon, achievementId |
| `AchievementUnlockResponse` | unlockedAchievements (list of AchievementResponse) |

### User Entity Changes
- Add `displayedAchievements` field — `@ElementCollection` list of achievement IDs (max 3)

### Database Migrations
- `V9__create_achievements_table.sql` — achievements table
- `V10__create_user_achievements_table.sql` — user_achievements join table
- `V11__add_displayed_achievements_to_users.sql` — user_displayed_achievements collection table
- `V12__seed_initial_achievements.sql` — INSERT 12 default achievements

### Repository Changes
- Create `AchievementRepository` — findAll, findByCategory, findByCode
- Create `UserAchievementRepository` — findByUserId, findByUserIdAndAchievementId, countByUserId

---

## 3. Business Logic / Services

| Service | Methods | Key Logic |
|---|---|---|
| `AchievementService` | `getAllAchievements(userId)` | Return all achievements with user's unlock status + progress |
| | `getUnlockedAchievements(userId)` | Return only unlocked, sorted by unlockedAt DESC |
| | `getAchievementStats(userId)` | Count unlocked, sum points |
| | `getDisplayedBadges(userId)` | Return user's displayed badge slots |
| | `updateDisplayedBadges(userId, ids)` | Validate all are unlocked, max 3, no duplicates |
| | `getFriendAchievements(userId, friendId)` | Validate friendship, return friend's unlocked |
| `AchievementUnlockService` | `checkAndUnlockAchievements(userId)` | Evaluate all achievement conditions for user |
| `AchievementProgressCalculator` | `calculateProgress(userId, achievement)` | Compute 0–100 progress per achievement type |

### Integration Points
- Call `checkAndUnlockAchievements()` after `ScoreCalculationService.recalculateScore()`
- Call `checkAndUnlockAchievements()` after `FriendRequestService.acceptRequest()`

### Exceptions
- `BadgeNotFoundException` — invalid achievement ID
- `BadgeNotUnlockedException` — trying to display a locked achievement

---

## 4. Client → Backend Story Mapping

| Client Story | Backend Dependencies | Can Parallel? |
|---|---|---|
| S-024 (Types/Service/Store) | All endpoints (mock mode) | ✅ Yes |
| S-025 (Catalog Screen) | `GET /achievements`, `GET /achievements/unlocked` | ✅ Yes — mock |
| S-026 (Stats Header) | `GET /achievements/stats` | ✅ Yes — mock |
| S-027 (Unlock Toast) | Unlock response in stat update | ✅ Yes — mock |
| S-028 (Badge Slots) | `GET /achievements/displayed`, `PUT /achievements/displayed` | ✅ Yes — mock |
| S-029 (Badge Modal) | None (pure UI, uses store data) | ✅ Yes |
| S-030 (Friend Achievements) | `GET /achievements/user/{id}`, `GET /achievements/user/{id}/stats` | ✅ Yes — mock |

---

## 5. Suggested Backend Sprint Ordering

| Order | Backend Work | Blocks Client Story |
|---|---|---|
| 1 | `Achievement` entity + `AchievementCategory` enum + migration V9 | All |
| 2 | `UserAchievement` entity + migration V10 | All |
| 3 | `AchievementRepository` + `UserAchievementRepository` | All |
| 4 | Seed achievements migration V12 | S-025 (real mode) |
| 5 | `AchievementResponse` + `AchievementStatsResponse` DTOs | S-025, S-026 |
| 6 | `AchievementService.getAllAchievements()` + `GET /achievements` | S-025 (real mode) |
| 7 | `AchievementService.getAchievementStats()` + `GET /achievements/stats` | S-026 (real mode) |
| 8 | `AchievementProgressCalculator` + `AchievementUnlockService` | S-027 (real mode) |
| 9 | Integration: unlock check in stat update + friend accept flows | S-027 (real mode) |
| 10 | `displayedAchievements` on User + migration V11 | S-028 (real mode) |
| 11 | `PUT /achievements/displayed` + `GET /achievements/displayed` | S-028 (real mode) |
| 12 | Friend achievement endpoints | S-030 (real mode) |

---

## 6. Notes

- **All client stories can proceed in parallel** — full mock mode support.
- The 12 seeded achievements should cover all 5 categories with varied requirement types.
- Achievement unlock checks should be lightweight — only check relevant achievements based on what changed (stat category, friend count, etc.).
- `displayedAchievements` on User is an `@ElementCollection` — store as a separate collection table for simplicity.
