# S7-04 — Activity Logging: POST /log + GET /history + GET /status

> **Status**: `NOT_STARTED`
> **Priority**: 🔴 Must
> **Blocks**: S-033

## Goal

Implement the core activity logging flow: validate cooldowns, persist log entry, award XP, check level-up, update streak.

## Tasks

### ActivityService
- [ ] Create `ActivityService` with:
  - `logActivity(UUID userId, UUID activityTypeId)`:
    1. Validate user exists
    2. Validate activity type exists and is active
    3. Check cooldown — query `ActivityLog` for existing entry within `cooldownHours`
    4. Persist `ActivityLog` entry
    5. Award XP via `XpService.awardXp()`
    6. Check level-up via `LevelService.checkAndUpdateLevel()`
    7. Update streak via `StreakService.recordActivity()` (S7-05)
    8. Return `ActivityLogResponse`
  - `getActivityHistory(UUID userId, Pageable pageable)` — paginated history
  - `getActivityStatus(UUID userId)` — today's logs + cooldown info

### Exceptions
- [ ] `ActivityTypeNotFoundException` extends `ApiException` (404)
- [ ] `ActivityCooldownException` extends `ApiException` (429 Too Many Requests) — with cooldown remaining info

### Controller Endpoints
- [ ] `POST /api/v1/activities/log` — body: `LogActivityRequest`, returns `ActivityLogResponse`
- [ ] `GET /api/v1/activities/history` — query: `page`, `size`, returns `Page<ActivityLogResponse>`
- [ ] `GET /api/v1/activities/status` — returns `ActivityStatusResponse`

### Cooldown Logic
- [ ] Query: find any `ActivityLog` where `userId` = ? AND `activityTypeId` = ? AND `loggedAt > (now - cooldownHours)`
- [ ] If found → throw `ActivityCooldownException` with `availableAt` timestamp

## Related Skills
- `conventions` — controller, service, exception patterns
- `error-handling` — custom exception hierarchy
