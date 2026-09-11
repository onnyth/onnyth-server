# S7-07 — Activity Feed: FeedService + GET /feed

> **Status**: `NOT_STARTED`
> **Priority**: 🔴 Must
> **Blocks**: S-043, S-044

## Goal

Create the activity feed system. Feed events are created by multiple sources (activity logs, level-ups, achievements, streaks) and queried to show a friends-only feed.

## Tasks

### FeedService
- [ ] Create `FeedService` with:
  - `createFeedEvent(UUID userId, FeedEventType type, Object eventData)` — persist a FeedEvent with JSON-serialized eventData
  - `getFriendFeed(UUID userId, Pageable pageable)` — paginated feed of events from user's friends (join with `Friendship` table)
  - Convert `FeedEvent` entities to `FeedEventResponse` DTOs (include user profile info)

### Feed Event Creation Integration Points
- [ ] In `ActivityService.logActivity()` → create `ACTIVITY` feed event
- [ ] In `LevelService.checkAndUpdateLevel()` → create `LEVEL_UP` feed event on level change
- [ ] In `StreakService.recordActivity()` → create `STREAK` feed event on milestone
- [ ] In `AchievementUnlockService` → create `ACHIEVEMENT` feed event on unlock

### Feed Query
- [ ] Custom JPQL/native query joining `feed_events` with `friendships`:
  ```sql
  SELECT fe.* FROM feed_events fe
  JOIN friendships f ON fe.user_id = f.friend_id AND f.user_id = :userId
  ORDER BY fe.created_at DESC
  ```

### Controller Endpoint
- [ ] `GET /api/v1/feed` — query: `page`, `size`, returns `Page<FeedEventResponse>`
- [ ] Create `FeedController`

## Related Skills
- `conventions` — controller, service patterns
- `data-layer` — friendship entity for join query
