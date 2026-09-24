# Leaderboard

## Status

Implemented

## Purpose

This feature provides the competitive, friend-scoped ranking surfaces in the backend: an overall leaderboard by total score, per-domain leaderboards, and a "my position" summary. It also maintains weekly snapshots so the overall friends leaderboard can show who moved up, dropped, or is newly appearing in the group.

## User Capabilities

- View their friends leaderboard, including themself.
- Filter that same friend cohort by one stat domain.
- See their current position, score gap to the next slot, and which friend is directly ahead.

## Business Rules

- The participant set is always `direct friends + current user`; there is no global/public leaderboard endpoint in this module.
- Overall ranking sorts participants by `users.totalScore` descending.
- Category ranking sorts the same participant set by the current domain score returned from the matching lifestat repository.
- Users with no current score for a requested category are ranked with value `0`.
- `GET /api/v1/leaderboard` defaults to `page=0`, `size=20`, and caps `size` at 50.
- `currentUserPosition` is 1-based.
- `pointsToNextPosition` is `score(ahead) - myScore`; it is `0` in first place.
- Weekly snapshots run on `0 0 0 * * SUN` and are taken only for users who currently have at least one friend.
- `positionChange` is computed as `oldPosition - currentPosition`, so positive numbers mean the user moved up.
- `isNew` is set only when an overall snapshot exists for the previous week and the user was absent from that snapshot.

## API

| Method | Path | Auth | Request | Response | Status |
|---|---|---|---|---|---|
| `GET` | `/api/v1/leaderboard` | JWT required | Query `page`, `size`, optional `category` (`StatDomain`) | Without `category`: `LeaderboardResponse`; with `category`: `Page<CategoryLeaderboardEntryResponse>` | `200` |
| `GET` | `/api/v1/leaderboard/my-position` | JWT required | — | `UserLeaderboardPositionResponse` (`position`, `totalParticipants`, `score`, `pointsToNextPosition`, `userAheadUsername`, `userAheadId`) | `200` |

## Data Model

| Table | Entity / owner | Key columns | Constraints / source |
|---|---|---|---|
| `leaderboard_snapshots` | `leaderboard/adapter/out/persistence/LeaderboardSnapshotEntity.java` | `id`, `user_id`, `friend_owner_id`, `position`, `score`, `snapshot_date`, `category` | Indexes on `(friend_owner_id, snapshot_date)` and `(user_id, snapshot_date)`; created by `src/main/resources/db/migration/V9__create_leaderboard_snapshots_table.sql` |
| `friendships` | `friendship/adapter/out/persistence/FriendshipEntity.java` | `user_id`, `friend_id` | Defines the leaderboard cohort; created by `src/main/resources/db/migration/V8__create_friendship_table.sql` |
| `users` | `user/adapter/out/persistence/UserEntity.java` | `id`, `total_score`, `rank_tier` | Overall ranking reads `total_score`; `rank_tier` is displayed in responses. Relevant score/rank additions come from `src/main/resources/db/migration/V4__add_total_score_to_users.sql` and `V5__add_rank_tier_to_users.sql` |

Live category responses are **not read from** `leaderboard_snapshots`; `LeaderboardUseCaseService` recomputes them from the current lifestat repositories when `/api/v1/leaderboard?category=...` is requested. The weekly snapshot job does persist separate per-domain snapshot rows for later comparison, but the current API does not surface that historical category data.

## Domain Logic

`leaderboard/application/usecase/LeaderboardUseCaseService.java` builds the live views: it loads `friendshipRepository.findFriendIdsByUserId(...)`, adds the current user if needed, fetches all matching `User` rows, and sorts them in memory. For category mode, it swaps `totalScore` for the requested per-domain score from the matching lifestat repository. `leaderboard/application/usecase/LeaderboardSnapshotUseCaseService.java` stores weekly overall + per-domain positions per friend owner, then `getFriendsLeaderboard()` uses the most recent overall snapshot to fill `positionChange` and `isNew`.

## Events

None.

## Dependencies

This feature depends on `friendship/` for cohort membership, `user/` for identity and total score, `lifestats/` for per-domain values, and Spring scheduling for weekly snapshots. No other feature currently calls leaderboard services directly.

## Key Files

| File | Role |
|---|---|
| `leaderboard/adapter/in/rest/LeaderboardController.java` | `/api/v1/leaderboard` API |
| `leaderboard/application/usecase/LeaderboardUseCaseService.java` | Live overall/category ranking and my-position logic |
| `leaderboard/application/usecase/LeaderboardSnapshotUseCaseService.java` | Weekly snapshot job and position-change lookup |
| `leaderboard/application/port/LeaderboardSnapshotRepository.java` | Snapshot persistence port |
| `leaderboard/adapter/out/persistence/{LeaderboardSnapshotEntity,LeaderboardSnapshotJpaRepository,LeaderboardSnapshotRepositoryAdapter}.java` | Snapshot table mapping and queries |
| `leaderboard/adapter/in/rest/dto/{LeaderboardResponse,LeaderboardEntryResponse,CategoryLeaderboardEntryResponse,UserLeaderboardPositionResponse}.java` | REST payloads |

## Known Limitations

- There is no global/public leaderboard endpoint. Earlier discovery/backlog material mentions global rankings, but the shipped controller only supports the authenticated user's friends-scoped cohort.
- `LeaderboardSnapshotUseCaseService` stores per-category snapshots too, but `LeaderboardUseCaseService.getLeaderboardByCategory()` does not read them, so category responses have no `positionChange` or `isNew` fields.

## Future Work

- `product-backlog.md` Epic 5.4 scopes "Trending users" as the next discovery-oriented leaderboard surface.
- The older `S3-01--leaderboard-system.md` global `/api/v1/leaderboards` idea remains unbuilt; current APIs are friends-only.
