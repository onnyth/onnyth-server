# XP

## Status

Implemented

## Purpose

Owns the atomic act of adding experience points to a user. The feature is intentionally small: it updates
`users.xp`, publishes an `XpAwardedEvent`, and lets other modules (currently `leveling/`) react to that
change.

## User Capabilities

- Earn XP by logging an activity through `/api/v1/activities/log`.
- Earn bonus XP automatically when a streak hits configured milestones.
- See the awarded activity XP amount in `ActivityLogResponse`, and see their total XP / level via the
  level-progress endpoint.

## Business Rules

- `XpUseCaseService.awardXp(userId, amount)` simply does `newTotalXp = user.getXp() + amount`, saves the
  `users` row, and publishes `XpAwardedEvent(userId, amount, newTotalXp)`.
- Current XP producers in the source:
  - `activity/application/usecase/ActivityUseCaseService.java`: awards `activity_type.xp_reward` after a
    successful activity log.
  - `streak/application/usecase/StreakUseCaseService.java`: awards milestone bonus XP at streak lengths
    `7=50`, `14=100`, `30=200`, `50=300`, `100=500`.
- Activity logging grants XP only after passing the activity cooldown check.
- There is no max XP, no decay, and no guard against negative `amount` values in `XpUseCaseService`.

## API

No dedicated `xp/` REST controller exists.

XP enters the system indirectly through `POST /api/v1/activities/log`, and the catalog of possible
activity XP rewards is exposed through `GET /api/v1/activities/types`.

## Data Model

| Table | Entity class | Key columns / constraints | Migration(s) |
|---|---|---|---|
| `users` | `user/adapter/out/persistence/UserEntity.java` | `xp BIGINT NOT NULL DEFAULT 0`; remote schema CHECK `xp >= 0` | `src/main/resources/db/migration/V16__add_xp_level_to_users.sql`; CHECK in `supabase/migrations/20260412183914_remote_schema.sql` |
| `activity_types` | `activity/adapter/out/persistence/ActivityTypeEntity.java` | `xp_reward INTEGER NOT NULL`; `cooldown_hours`; `is_active` | `src/main/resources/db/migration/V14__create_activity_types_table.sql`, `src/main/resources/db/migration/V20__seed_activity_types.sql`, `supabase/migrations/20260507_seed_activity_types.sql` |
| `activity_log` | `activity/adapter/out/persistence/ActivityLogEntity.java` | `xp_earned INTEGER NOT NULL`; indexed by user and `(user, activity type, logged_at)` | `src/main/resources/db/migration/V15__create_activity_log_table.sql` |

## Domain Logic

```text
ActivityController.POST /activities/log
  -> ActivityUseCaseService.logActivity(...)
  -> persist activity_log row
  -> XpUseCaseService.awardXp(userId, activityType.xpReward)
  -> publish XpAwardedEvent
  -> LevelUseCaseService.onXpAwarded(...) may update level
  -> StreakUseCaseService.recordActivity(...) may award a second XP grant on milestone days
```

The XP feature itself does not know why XP was awarded; it only persists the new total and emits the event.

## Events

- **Publishes** `XpAwardedEvent(UUID userId, int xpAmount, long newTotalXp)`.
- **Consumed by** `leveling/application/usecase/LevelUseCaseService.java`.

## Dependencies

- **`user/`** — owns `users.xp`.
- **`activity/`** — main user-facing entrypoint for normal XP awards.
- **`streak/`** — secondary producer for milestone bonus XP.
- **`leveling/`** — reacts to `XpAwardedEvent`.

## Key Files

| File | Role |
|---|---|
| `xp/application/usecase/XpUseCaseService.java` | Atomic XP mutation and event publication |
| `xp/domain/event/XpAwardedEvent.java` | Cross-module XP event contract |
| `activity/application/usecase/ActivityUseCaseService.java` | Main caller for activity-earned XP |
| `streak/application/usecase/StreakUseCaseService.java` | Milestone-bonus XP caller |
| `activity/adapter/in/rest/ActivityController.java` | User-facing `/api/v1/activities/*` endpoints that expose XP-producing flows |

## Known Limitations

- `ActivityLogResponse.newTotalXP` is captured **before** `StreakUseCaseService.recordActivity(...)` runs, so
  milestone-day responses omit the extra streak bonus XP.
- `ActivityLogResponse.newLevel` / `levelTitle` are also assembled before any streak-milestone bonus XP can
  trigger a second level-up.
- `src/main/resources/db/migration/V20__seed_activity_types.sql` still seeds legacy categories
  (`FITNESS`, `EDUCATION`, `SOCIAL_INFLUENCE`, `CAREER`) that do not match `shared.domain.model.StatDomain`
  (`PHYSIQUE`, `WISDOM`, `CHARISMA`, `OCCUPATION`, `WEALTH`). The parallel Supabase seed partially updates
  this (`OCCUPATION` is fixed there), but still keeps old names like `FITNESS` / `EDUCATION` /
  `SOCIAL_INFLUENCE`, so enum-mapping risk remains in the activity/XP path.
- `XpUseCaseService` does not validate `amount >= 0`; callers currently pass positive values, but the
  service itself does not enforce that invariant.

## Future Work

Sprint 7's progression planning ties XP more tightly to activity-feed style events (`S-044`), but no
additional XP-specific backlog item is currently scoped beyond the existing activity/streak integrations.
