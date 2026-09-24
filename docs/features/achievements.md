# Achievements

## Status

Partially Implemented
The read APIs and badge-display flow are wired, but unlock evaluation is only triggered on friend acceptance and several seeded achievement definitions no longer match the current codebase.

## Purpose

This feature exposes the achievement catalog, per-user unlock/progress state, summary stats, displayed badge slots, and friend-only achievement views. It is designed to reward stat growth, social actions, milestones, and other long-term progression signals.

## User Capabilities

- View the active achievement catalog with unlock state and progress percentage.
- Filter achievements by category.
- View only unlocked achievements.
- See aggregate counts and points earned.
- Choose up to 3 unlocked badges to display.
- View a friend's unlocked achievements and achievement stats.

## Business Rules

- Achievement categories in code are `STATS`, `SOCIAL`, `STREAK`, `MILESTONE`, and `SPECIAL`.
- `getAllAchievements()` only returns `is_active = true` achievements.
- `getUnlockedAchievements()` sorts by `unlockedAt` descending.
- `updateDisplayedBadges()` de-duplicates the submitted ids, rejects lists longer than 3, verifies every id exists, and verifies every id is already unlocked by the user.
- Friend-achievement endpoints require an existing friendship.
- Progress calculation is requirement-type driven:
  - `STAT_VALUE_<DOMAIN>` → `min(100, domainScore * 100 / threshold)`
  - `ALL_STATS_MIN` → percentage of the 5 current `StatDomain` values meeting the threshold
  - `FRIEND_COUNT` → `min(100, friendCount * 100 / threshold)`
  - `TOTAL_SCORE` → `min(100, totalScore * 100 / threshold)`
  - `RANK_TIER` → `min(100, rankTier.ordinal() * 100 / threshold)`
  - `PROFILE_COMPLETE` → `100` if `user.profileComplete == true`, otherwise `0`
  - `ANY_STAT_INPUT` → `100` once any current stat domain score is above `0`
  - `UPDATE_STREAK` → currently hard-coded to `0`
- The unlock engine only persists a `user_achievements` row when progress reaches `>= 100` and the user has not already unlocked that achievement.

## API

| Method | Path | Auth | Request | Response | Status |
|---|---|---|---|---|---|
| `GET` | `/api/v1/achievements` | JWT required | — | `List<AchievementResponse>` | `200` |
| `GET` | `/api/v1/achievements/category/{category}` | JWT required | Path `category` (`AchievementCategory`) | `List<AchievementResponse>` | `200` |
| `GET` | `/api/v1/achievements/unlocked` | JWT required | — | `List<AchievementResponse>` | `200` |
| `GET` | `/api/v1/achievements/stats` | JWT required | — | `AchievementStatsResponse` | `200` |
| `GET` | `/api/v1/achievements/displayed` | JWT required | — | `List<DisplayedBadgeResponse>` | `200` |
| `PUT` | `/api/v1/achievements/displayed` | JWT required | `DisplayedBadgeRequest` (`achievementIds`, max 3) | `List<DisplayedBadgeResponse>` | `200 / 400 / 404` |
| `GET` | `/api/v1/achievements/user/{friendId}` | JWT required | Path `friendId` | `List<AchievementResponse>` | `200 / 400` |
| `GET` | `/api/v1/achievements/user/{friendId}/stats` | JWT required | Path `friendId` | `AchievementStatsResponse` | `200 / 400` |

## Data Model

| Table | Entity / owner | Key columns | Constraints / source |
|---|---|---|---|
| `achievements` | `achievement/adapter/out/persistence/AchievementEntity.java` | `id`, `code`, `name`, `description`, `icon`, `category`, `requirement_type`, `threshold`, `points`, `is_active` | `code` is unique; created by `src/main/resources/db/migration/V10__create_achievements_table.sql`; initial 12 rows seeded by `V13__seed_initial_achievements.sql` |
| `user_achievements` | `achievement/adapter/out/persistence/UserAchievementEntity.java` | `id`, `user_id`, `achievement_id`, `unlocked_at` | Unique `(user_id, achievement_id)`; created by `src/main/resources/db/migration/V11__create_user_achievements_table.sql` |
| `user_displayed_achievements` | `user/adapter/out/persistence/UserEntity.java` `@ElementCollection` | `user_id`, `achievement_id` | PK `(user_id, achievement_id)`; created by `src/main/resources/db/migration/V12__add_displayed_achievements_to_users.sql` |

## Domain Logic

`achievement/application/usecase/AchievementUseCaseService.java` handles all read APIs plus displayed-badge updates. It loads the user's unlock map once, uses `AchievementProgressCalculator` for still-locked rows, and produces `AchievementResponse` or `AchievementStatsResponse` DTOs. `achievement/application/usecase/AchievementUnlockUseCaseService.java` is the write side: it scans all active achievements not yet unlocked by the user, computes progress, and inserts `UserAchievement` rows when the requirement is satisfied.

## Events

None. The feature defines no Spring event type, has no `@EventListener`, and does not publish an achievement-unlocked event. Unlock checks are invoked synchronously from `friendship/application/usecase/FriendshipUseCaseService.java`.

## Dependencies

This feature depends on `user/`, `friendship/`, `lifestats/`, and `ranking/` to compute progress and protect friend-only views. Profile-card style features consume the displayed-badge list stored on the user, and `feed/` was intended to consume unlocks but is not wired.

## Key Files

| File | Role |
|---|---|
| `achievement/adapter/in/rest/AchievementController.java` | `/api/v1/achievements/**` REST API |
| `achievement/application/usecase/AchievementUseCaseService.java` | Catalog, stats, displayed badges, and friend views |
| `achievement/application/usecase/AchievementUnlockUseCaseService.java` | Unlock evaluator / persister |
| `achievement/application/AchievementProgressCalculator.java` | Requirement-type progress formulas |
| `achievement/adapter/out/persistence/{AchievementEntity,UserAchievementEntity}.java` | Table mappings |
| `achievement/adapter/out/persistence/{AchievementJpaRepository,UserAchievementJpaRepository}.java` | JPA query layer |
| `achievement/domain/model/{Achievement,AchievementCategory,UserAchievement}.java` | Core domain models |
| `achievement/adapter/in/rest/dto/{AchievementResponse,AchievementStatsResponse,DisplayedBadgeRequest,DisplayedBadgeResponse}.java` | REST payloads |

## Known Limitations

- `achievement/application/usecase/AchievementUnlockUseCaseService.java` is called only from `friendship/application/usecase/FriendshipUseCaseService.java` after friend-request acceptance. Stat changes, score changes, profile completion changes, level changes, and streak updates do not automatically trigger achievement evaluation.
- `src/main/resources/db/migration/V13__seed_initial_achievements.sql` still seeds `STAT_VALUE_CAREER` and `STAT_VALUE_FITNESS`, but `achievement/application/AchievementProgressCalculator.java` expects current `StatDomain` enum names like `OCCUPATION` and `PHYSIQUE`. Those two seeded stat achievements therefore never progress.
- The same seed file uses `UPDATE_STREAK` for the two streak achievements, while `AchievementProgressCalculator` currently hard-codes `case "UPDATE_STREAK" -> 0`; both streak achievements are permanently locked.
- Seeded achievement `RANK_GOLD` uses threshold `3`, but `calculateRankTierProgress()` multiplies `rankTier.ordinal()` by 100 and divides by the threshold. With the current `RankTier` enum ordering, the achievement reaches 100% at `PLATINUM` (ordinal 3), not at `GOLD` (ordinal 2).

## Future Work

- Finish the originally-planned integration: run unlock evaluation after score/stat changes and streak changes, not just friendship acceptance.
- If `feed/` is completed, emit achievement feed items as planned in `S7-07--activity-feed.md`.
