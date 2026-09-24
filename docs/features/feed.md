# Feed

## Status

Partially Implemented
The read API, table, and service exist, but no application code calls `FeedUseCaseService.createFeedEvent(...)`, so the backend does not currently populate the feed from activity, leveling, achievement, or streak flows.

## Purpose

This feature is meant to provide a friends-only activity feed so users can see what their network has been doing recently. The storage and query side is present: feed rows can be persisted as typed events with JSON payloads and queried back with friend and profile filtering.

## User Capabilities

- Fetch a paginated feed of events created by direct friends.

## Business Rules

- The feed query returns only rows whose `feed_events.user_id` belongs to the current user's direct friends; it does not include the current user's own events.
- `GET /api/v1/feed` defaults to `page=0` and `size=20`; unlike friends/user search/leaderboard, the controller does not cap `size`.
- Supported event kinds in code are `ACTIVITY`, `LEVEL_UP`, `ACHIEVEMENT`, and `STREAK`.
- `FeedUseCaseService.createFeedEvent(...)` JSON-serializes arbitrary `eventData`; if serialization fails, it stores `{}`.
- `FeedEventResponse.eventData` is returned as a raw JSON string, not as a typed nested DTO.
- Responses are enriched with `username` and `profilePic` by batch-loading the distinct event owners from `users`.

## API

| Method | Path | Auth | Request | Response | Status |
|---|---|---|---|---|---|
| `GET` | `/api/v1/feed` | JWT required | Query `page`, `size` | `Page<FeedEventResponse>` (`id`, `userId`, `username`, `profilePic`, `eventType`, `eventData`, `createdAt`) | `200` |

## Data Model

| Table | Entity / owner | Key columns | Constraints / source |
|---|---|---|---|
| `feed_events` | `feed/adapter/out/persistence/FeedEventEntity.java` | `id`, `user_id`, `event_type`, `event_data`, `created_at` | Flyway creates the table in `src/main/resources/db/migration/V18__create_feed_events_table.sql`; that file defines `event_data` as `TEXT`, while the JPA entity maps it as JSON (`@JdbcTypeCode(SqlTypes.JSON)`, `columnDefinition = "jsonb"`) |
| `friendships` | `friendship/adapter/out/persistence/FriendshipEntity.java` | `user_id`, `friend_id` | Used by the custom friend-feed query; created by `src/main/resources/db/migration/V8__create_friendship_table.sql` |
| `users` | `user/adapter/out/persistence/UserEntity.java` | `id`, `username`, `profile_pic` | Used only to enrich feed rows with lightweight profile info |

## Domain Logic

`feed/application/usecase/FeedUseCaseService.java` has two code paths: `createFeedEvent(...)` persists a `FeedEvent` with typed `FeedEventType` and serialized `eventData`, while `getFriendFeed(...)` delegates to `feed/adapter/out/persistence/FeedEventJpaRepository.java`, whose JPQL query joins `feed_events` to `friendships` by `friendId`. The repository returns events newest-first, and the use case then batch-loads the corresponding users so the API can include usernames and profile pictures without N+1 queries.

## Events

None. This module defines a `FeedEventType` enum, but it does not publish or consume Spring `ApplicationEvent`s, and there are no listeners or call sites wiring activity/level-up/achievement/streak changes into `createFeedEvent(...)`.

## Dependencies

This feature depends on `friendship/` for friend scoping, `user/` for profile enrichment, and Jackson for JSON serialization. `activity/`, `leveling/`, `achievement/`, and `streak/` are the intended producers, but they do not currently call it.

## Key Files

| File | Role |
|---|---|
| `feed/adapter/in/rest/FeedController.java` | `/api/v1/feed` endpoint |
| `feed/application/usecase/FeedUseCaseService.java` | Event creation + friend-feed query orchestration |
| `feed/domain/model/{FeedEvent,FeedEventType}.java` | Core feed row model and supported event types |
| `feed/application/port/FeedEventRepository.java` | Hexagonal persistence port |
| `feed/adapter/out/persistence/{FeedEventEntity,FeedEventJpaRepository,FeedEventRepositoryAdapter}.java` | Table mapping and friend-feed query |
| `feed/adapter/in/rest/dto/FeedEventResponse.java` | REST payload |

## Known Limitations

- No source file outside `feed/application/usecase/FeedUseCaseService.java` calls `createFeedEvent(...)`, so the application never inserts feed rows.
- `src/main/resources/db/migration/V18__create_feed_events_table.sql` creates `event_data` as `TEXT`, but `feed/adapter/out/persistence/FeedEventEntity.java` maps it as JSONB. The Supabase schema snapshot (`supabase/migrations/20260412183914_remote_schema.sql`) uses JSONB, so Flyway and JPA are out of sync.

## Future Work

- Finish the originally-planned producer wiring: create `ACTIVITY`, `LEVEL_UP`, `ACHIEVEMENT`, and `STREAK` rows from the relevant features (see `docs/development/known-issues.md` #6).
- Once follow/unfollow becomes real (`product-backlog.md` Epic 4.4 / 6.3), the feed can also absorb follow-based social events if desired.
