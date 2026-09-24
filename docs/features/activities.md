# Activities

## Status

Partially Implemented
Controllers and use cases exist, but the seeded activity catalog in `V20__seed_activity_types.sql` still uses pre-migration category names that do not match `shared/domain/model/StatDomain`.

## Purpose

This feature lets users log repeatable real-world activities for XP, level progression, and streak tracking. It also exposes the activity-type catalog, personal activity history, current cooldown status, current level progress, and streak summary under one API surface.

## User Capabilities

- Browse active activity types, optionally filtered by category.
- Log an activity by `activityTypeId`.
- View paginated activity history.
- View today's logged activities and currently active cooldowns.
- View current level progress.
- View current/longest streak and next streak milestone.

## Business Rules

- Activity categories in code are the current `StatDomain` enum values: `OCCUPATION`, `WEALTH`, `PHYSIQUE`, `WISDOM`, and `CHARISMA`.
- Cooldowns are hour-based, not calendar-based: an activity is blocked whenever another log for the same `activityTypeId` exists after `Instant.now().minus(cooldownHours, HOURS)`.
- A cooldown hit throws `ActivityCooldownException` and computes `availableAt = recentLog.loggedAt + cooldownHours`.
- `logActivity()` persists an `activity_log` row before it awards XP.
- XP awards go through `xp/application/usecase/XpUseCaseService.java`, which publishes `XpAwardedEvent(userId, xpAmount, newTotalXp)`.
- `leveling/application/usecase/LevelUseCaseService.java` consumes that event and recalculates level with `xpForLevel(level) = 100 + floor((level - 1) / 5) * 50`.
- Level titles are: `Novice` (<5), `Apprentice` (5-9), `Journeyman` (10-19), `Adept` (20-29), `Expert` (30-39), `Master` (40-49), `Grandmaster` (50+).
- Streaks are date-based, not activity-count-based. Logging on consecutive days increments the streak; missing a day resets it to 1.
- Streak milestones are 7, 14, 30, 50, and 100 days, with bonus XP rewards 50, 100, 200, 300, and 500 respectively.
- `GET /api/v1/activities/status` defines "today" using UTC day boundaries (`today.atStartOfDay(ZoneOffset.UTC)`).
- The controller defaults history to `page=0`, `size=20` and does not cap `size`.

## API

| Method | Path | Auth | Request | Response | Status |
|---|---|---|---|---|---|
| `GET` | `/api/v1/activities/types` | JWT required | Optional query `category` (`StatDomain`) | `List<ActivityTypeResponse>` | `200` |
| `POST` | `/api/v1/activities/log` | JWT required | `LogActivityRequest` (`activityTypeId`) | `ActivityLogResponse` (`activityType`, `xpEarned`, `newTotalXP`, `newLevel`, `levelTitle`, `streakUpdated`) | `200 / 404 / 429` |
| `GET` | `/api/v1/activities/history` | JWT required | Query `page`, `size` | `Page<ActivityLogResponse>` | `200` |
| `GET` | `/api/v1/activities/status` | JWT required | — | `ActivityStatusResponse` (`todayLogs`, `cooldowns[]`) | `200` |
| `GET` | `/api/v1/activities/level` | JWT required | — | `LevelProgressResponse` | `200` |
| `GET` | `/api/v1/activities/streaks` | JWT required | — | `StreakResponse` | `200` |

## Data Model

| Table | Entity / owner | Key columns | Constraints / source |
|---|---|---|---|
| `activity_types` | `activity/adapter/out/persistence/ActivityTypeEntity.java` | `id`, `name`, `description`, `icon`, `category`, `xp_reward`, `frequency`, `cooldown_hours`, `is_active`, `created_at` | Indexes on `category` and `is_active`; created by `src/main/resources/db/migration/V14__create_activity_types_table.sql`; 25 rows seeded by `V20__seed_activity_types.sql` |
| `activity_log` | `activity/adapter/out/persistence/ActivityLogEntity.java` | `id`, `user_id`, `activity_type_id`, `xp_earned`, `logged_at` | Indexes on `user_id` and `(user_id, activity_type_id, logged_at)`; created by `src/main/resources/db/migration/V15__create_activity_log_table.sql` |
| `users` | `user/adapter/out/persistence/UserEntity.java` | `id`, `xp`, `level` | XP/level columns added by `src/main/resources/db/migration/V16__add_xp_level_to_users.sql` |
| `user_streaks` | `streak/adapter/out/persistence/UserStreakEntity.java` | `id`, `user_id`, `current_streak`, `longest_streak`, `last_activity_date` | Unique `user_id`; created by `src/main/resources/db/migration/V17__create_user_streaks_table.sql` |

## Domain Logic

`activity/application/usecase/ActivityTypeUseCaseService.java` exposes the active catalog. `activity/application/usecase/ActivityUseCaseService.java` performs the write path: load the user, load the active type, enforce cooldown, insert an `ActivityLog`, award XP, read back the user for level fields, then update streak state. History maps persisted logs back to their current `ActivityType`, while `getActivityStatus()` loads only today's logs, converts them into `todayLogs`, and derives `cooldowns` by re-applying each type's cooldown window.

## Events

The activity module itself does not declare a custom event type, but its main write path indirectly publishes `XpAwardedEvent(UUID userId, int xpAmount, long newTotalXp)` by calling `xp/application/usecase/XpUseCaseService.java`. `leveling/application/usecase/LevelUseCaseService.java` consumes that event and may then publish `LevelUpEvent(UUID userId, int oldLevel, int newLevel, String newTitle)`.

## Dependencies

This feature depends on `user/`, `xp/`, `leveling/`, and `streak/`. `feed/` was intended to depend on activity/streak/level changes, but that producer wiring is still absent.

## Key Files

| File | Role |
|---|---|
| `activity/adapter/in/rest/ActivityController.java` | `/api/v1/activities/**` REST API |
| `activity/application/usecase/ActivityTypeUseCaseService.java` | Active catalog lookup |
| `activity/application/usecase/ActivityUseCaseService.java` | Log/history/status orchestration |
| `activity/adapter/out/persistence/{ActivityTypeEntity,ActivityLogEntity}.java` | Table mappings |
| `activity/adapter/out/persistence/{ActivityTypeJpaRepository,ActivityLogJpaRepository}.java` | JPA queries |
| `activity/domain/model/{ActivityType,ActivityLog,ActivityFrequency}.java` | Core domain models |
| `leveling/application/usecase/LevelUseCaseService.java` | XP-to-level progression exposed at `/activities/level` |
| `streak/application/usecase/StreakUseCaseService.java` | Date-based streak + milestone rewards |

## Known Limitations

- `src/main/resources/db/migration/V20__seed_activity_types.sql` still seeds obsolete category names (`FITNESS`, `EDUCATION`, `SOCIAL_INFLUENCE`, `CAREER`, `WEALTH`), while `activity/adapter/out/persistence/ActivityTypeEntity.java` and the controller expect current `StatDomain` values like `OCCUPATION` and `PHYSIQUE`.
- `getActivityStatus()` builds cooldowns only from logs that happened during the current UTC day. A weekly activity logged yesterday can still be on cooldown for several more days, but it will not appear in the `cooldowns` list.
- `ActivityLogResponse.newTotalXP` and `newLevel` are captured before `streak/application/usecase/StreakUseCaseService.java` can award milestone bonus XP. On a milestone day, the response can under-report the user's final XP and level.

## Future Work

- `ACTIVITY`, `LEVEL_UP`, and milestone `STREAK` feed creation was originally planned; that wiring is still missing from the source (see `docs/features/feed.md`).
