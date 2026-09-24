# Leveling

## Status

Implemented

## Purpose

Maps accumulated XP into a long-running player level and title. The feature exposes progress to the next
level, updates `users.level` whenever XP crosses a threshold, and emits a `LevelUpEvent` that downstream
features can react to later.

## User Capabilities

- Fetch current level, title, total XP, next-level threshold, and percentage progress.
- See level and title on profile cards.
- Receive updated level/title metadata in activity-log responses after XP-granting actions.

## Business Rules

- XP curve from `LevelUseCaseService.xpForLevel(level)`: `100 + floor((level - 1) / 5) * 50`.
  That means:

  | Current level range | XP needed to reach the next level |
  |---|---:|
  | 1-5 | 100 |
  | 6-10 | 150 |
  | 11-15 | 200 |
  | 16-20 | 250 |
  | 21-25 | 300 |
  | ... | increases by 50 every 5 levels |

- Cumulative threshold is `totalXpForLevel(level) = sum(xpForLevel(1..level-1))`. Examples: level 2 at
  100 total XP, level 6 at 500, level 11 at 1250, level 21 at 3500, level 51 at 16250.
- `calculateLevel(totalXp)` repeatedly subtracts the per-level requirement until the remaining XP no longer
  covers the next step; there is no hard cap.
- Title mapping:

  | Level range | Title |
  |---|---|
  | 1-4 | `Novice` |
  | 5-9 | `Apprentice` |
  | 10-19 | `Journeyman` |
  | 20-29 | `Adept` |
  | 30-39 | `Expert` |
  | 40-49 | `Master` |
  | 50+ | `Grandmaster` |

- `LevelProgressResponse.xpForNextLevel` is the **absolute total-XP threshold** for the next level, not the
  delta remaining.
- `progressPercent` is rounded to one decimal place from
  `xpIntoCurrentLevel / xpNeededForCurrentBand * 100`.

## API

| Method | Path | Auth | Request | Response | Status |
|---|---|---|---|---|---|
| `GET` | `/api/v1/activities/level` | JWT | — | `LevelProgressResponse { currentLevel, title, currentXP, xpForNextLevel, progressPercent }` | 200 |

Related read surfaces: `ProfileCardResponse` also exposes `level` and `levelTitle`.

## Data Model

| Table | Entity class | Key columns / constraints | Migration(s) |
|---|---|---|---|
| `users` | `user/adapter/out/persistence/UserEntity.java` | `xp BIGINT NOT NULL DEFAULT 0`; `level INTEGER NOT NULL DEFAULT 1`; remote schema CHECKs `xp >= 0` and `level >= 1` | `src/main/resources/db/migration/V16__add_xp_level_to_users.sql`; CHECKs visible in `supabase/migrations/20260412183914_remote_schema.sql` |

## Domain Logic

```text
XpUseCaseService.awardXp(userId, amount)
  -> users.xp += amount
  -> publish XpAwardedEvent(userId, amount, newTotalXp)

LevelUseCaseService.onXpAwarded(event)
  -> checkAndUpdateLevel(event.userId())
  -> calculateLevel(user.xp)
  -> if newLevel > oldLevel: save users.level and publish LevelUpEvent
```

`getLevelProgress(...)` is a pure read-side calculation over the stored `users.xp` and `users.level`.
`ProfileCardResponse.fromUser(...)` derives `levelTitle` by calling `LevelUseCaseService.getTitle(user.getLevel())`.

## Events

- **Consumes** `XpAwardedEvent(UUID userId, int xpAmount, long newTotalXp)`.
- **Publishes** `LevelUpEvent(UUID userId, int oldLevel, int newLevel, String newTitle)`.

## Dependencies

- **`xp/`** — the only current producer of XP changes.
- **`user/`** — owns the stored `xp` and `level` columns.
- **Consumers**: `activity/` exposes the progress endpoint; `profile/` reads `level` for profile cards.

## Key Files

| File | Role |
|---|---|
| `leveling/application/usecase/LevelUseCaseService.java` | XP curve, title mapping, level-up listener, progress DTO assembly |
| `leveling/domain/event/LevelUpEvent.java` | Event emitted when `users.level` increases |
| `leveling/adapter/in/rest/dto/LevelProgressResponse.java` | Public response DTO |
| `activity/adapter/in/rest/ActivityController.java` | `/api/v1/activities/level` endpoint |
| `profile/adapter/in/rest/dto/ProfileCardResponse.java` | Level/title fields surfaced in profile cards |

## Known Limitations

- The comment examples inside `LevelUseCaseService.xpForLevel(...)` do not match the implementation; the
  formula is correct, but the inline examples are stale.
- `LevelUpEvent` is published, but no listener in the current source consumes it to create feed items,
  notifications, or achievements.
- The endpoint lives under `/api/v1/activities/level`, not the `/api/v1/profile/level` path suggested in
  Sprint 7 planning docs.

## Future Work

Sprint 7 planning still mentions a dedicated `/api/v1/profile/level` surface (`S-039`) plus feed integration
for level-up events (`S-044`), neither of which is implemented in current code.
