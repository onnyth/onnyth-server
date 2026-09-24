# Streak

## Status

Partially Implemented
Daily streak counting and milestone XP bonuses work, but the planned streak milestone event/feed integration
is not present.

## Purpose

Tracks whether a user is maintaining consecutive daily activity. It records the current and longest streak,
exposes the next milestone reward, and hooks streak milestones into XP progression.

## User Capabilities

- Grow a streak by logging at least one activity on consecutive days.
- Fetch current streak, longest streak, last active day, whether the streak is still alive, and the next
  milestone reward.
- Receive bonus XP automatically when a milestone streak length is reached.

## Business Rules

- `recordActivity(userId)` uses `LocalDate.now()` and applies exactly one streak update per calendar day:
  - if `lastActivityDate == today` → no-op, return `false`.
  - if `lastActivityDate == today - 1 day` → `currentStreak++`.
  - otherwise (first log or broken streak) → `currentStreak = 1`.
- `longestStreak = max(longestStreak, currentStreak)` after each successful daily update.
- Milestone days and rewards are hard-coded:

  | Streak days | Bonus XP |
  |---|---:|
  | 7 | 50 |
  | 14 | 100 |
  | 30 | 200 |
  | 50 | 300 |
  | 100 | 500 |

- `StreakResponse.isActive` is `true` when `lastActivityDate` is either today or yesterday.
- `nextMilestone` is the first configured milestone greater than `currentStreak`; if none exists, it is
  `null` and `nextMilestoneReward=0`.

## API

| Method | Path | Auth | Request | Response | Status |
|---|---|---|---|---|---|
| `POST` | `/api/v1/activities/log` | JWT | `LogActivityRequest { activityTypeId }` | `ActivityLogResponse { ..., streakUpdated }` | 200 / 404 / 429 |
| `GET` | `/api/v1/activities/streaks` | JWT | — | `StreakResponse { currentStreak, longestStreak, lastActivityDate, isActive, nextMilestone, nextMilestoneReward }` | 200 |

## Data Model

| Table | Entity class | Key columns / constraints | Migration(s) |
|---|---|---|---|
| `user_streaks` | `streak/adapter/out/persistence/UserStreakEntity.java` | PK `id`; unique `user_id`; `current_streak` / `longest_streak` default 0; `last_activity_date` nullable | `src/main/resources/db/migration/V17__create_user_streaks_table.sql` |

Note: the Supabase schema snapshot also contains `user_streaks.updated_at`, but the Flyway migration and
JPA entity shown above do not map it.

## Domain Logic

```text
POST /api/v1/activities/log
  -> ActivityUseCaseService.logActivity(...)
  -> StreakUseCaseService.recordActivity(userId)
     -> load or create user_streaks row
     -> update current/longest streak based on lastActivityDate
     -> save row
     -> if milestone: XpUseCaseService.awardXp(userId, bonusXp)
```

`GET /api/v1/activities/streaks` is a pure repository read with derived `isActive`, `nextMilestone`, and
`nextMilestoneReward` values.

## Events

None. No `StreakMilestoneEvent`, no `FeedEventType.STREAK` publisher, and no Spring `ApplicationEvent`
publication exist in the current streak code.

## Dependencies

- **`activity/`** — the only current user-facing mutator of streaks.
- **`xp/`** — receives milestone bonus awards.
- **`profile/`** — reads `currentStreak` into `ProfileCardResponse`.

## Key Files

| File | Role |
|---|---|
| `streak/application/usecase/StreakUseCaseService.java` | Streak counting rules, milestone rewards, DTO assembly |
| `streak/domain/model/UserStreak.java` | Domain model for per-user streak state |
| `streak/application/port/UserStreakRepository.java` | Outbound port |
| `streak/adapter/out/persistence/{UserStreakEntity,UserStreakJpaRepository,UserStreakRepositoryAdapter,UserStreakPersistenceMapper}.java` | JPA persistence |
| `streak/adapter/in/rest/dto/StreakResponse.java` | Public response DTO |
| `activity/adapter/in/rest/ActivityController.java` | `/api/v1/activities/streaks` endpoint and indirect `/log` write path |

## Known Limitations

- Sprint 7 planned a streak milestone feed/event, and `feed/domain/model/FeedEventType.java` already has a
  `STREAK` enum value, but `StreakUseCaseService` only grants bonus XP and publishes nothing.
- `recordActivity()` uses the server's default `LocalDate.now()`, while `ActivityUseCaseService.getActivityStatus()`
  computes "today" using UTC day boundaries. Around midnight / timezone differences, streak state and
  `/api/v1/activities/status` can disagree.
- On milestone days, `ActivityLogResponse` still reports the pre-bonus XP total and pre-bonus level/title,
  because the response is assembled before the streak bonus XP award runs.

## Future Work

Sprint story `S7-05--streak-system.md` explicitly called for publishing a streak milestone feed event.
That follow-up is still not built in current code.
