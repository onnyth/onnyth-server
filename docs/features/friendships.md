# Friendships

## Status

Partially Implemented
Friend requests, friendships, friends list/search, and friend profile comparison are wired; the `Follow` model is persistence-only and has no use case or REST endpoint.

## Purpose

This feature manages Onnyth's direct-friend graph: sending requests, accepting/rejecting them, listing friends, and removing connections. It also powers friend-only profile comparison so a user can compare total score and per-domain strengths against an existing friend.

## User Capabilities

- Send a friend request to another user.
- View pending requests they received or sent.
- See a pending-request badge count.
- Accept or reject a received request.
- List friends with pagination and server-side sorting.
- Search within their existing friends by username or full name.
- Remove an existing friend.
- View a friend's profile summary, rank progress, and stat comparison.

## Business Rules

- A user cannot send a friend request to themself.
- Both sender and receiver must already exist in `users`.
- A request is blocked if the two users are already friends.
- A request is also blocked if a `PENDING` request already exists in either direction.
- Only the receiver of a request can accept or reject it.
- Only `PENDING` requests can transition to `ACCEPTED` or `REJECTED`.
- Accepting a request creates **two** `friendships` rows (`A -> B` and `B -> A`) for fast directional queries.
- Removing a friend deletes both directional friendship rows.
- Friend-profile access is guarded by `friendshipRepository.existsByUserIdAndFriendId(...)`; non-friends get `NotFriendsException`.
- `scoreDifference` in the comparison response is `currentUser.totalScore - friend.totalScore`.
- Per-domain comparison iterates the current `StatDomain` set (`OCCUPATION`, `WEALTH`, `PHYSIQUE`, `WISDOM`, `CHARISMA`) and reports only which display names the current user is higher/lower in.
- `GET /api/v1/friends` defaults to `page=0`, `size=20`, `sort=createdAt`, and caps `size` at 50.

## API

| Method | Path | Auth | Request | Response | Status |
|---|---|---|---|---|---|
| `POST` | `/api/v1/friends/request/{userId}` | JWT required | Path `userId` | `FriendRequestResponse` (`requestId`, sender/receiver ids + usernames, `status`, `createdAt`) | `200 / 404 / 409` |
| `GET` | `/api/v1/friends/requests/received` | JWT required | — | `List<FriendRequestResponse>` | `200` |
| `GET` | `/api/v1/friends/requests/sent` | JWT required | — | `List<FriendRequestResponse>` | `200` |
| `GET` | `/api/v1/friends/requests/count` | JWT required | — | `{"count": <long>}` | `200` |
| `PUT` | `/api/v1/friends/request/{requestId}/accept` | JWT required | Path `requestId` | `FriendRequestResponse` | `200 / 403 / 404 / 409` |
| `PUT` | `/api/v1/friends/request/{requestId}/reject` | JWT required | Path `requestId` | `FriendRequestResponse` | `200 / 403 / 404 / 409` |
| `GET` | `/api/v1/friends` | JWT required | Query `sort`, `page`, `size` | `Page<FriendResponse>` (`userId`, `username`, `fullName`, `profilePic`, `rankTier`, `totalScore`, `friendSince`) | `200` |
| `GET` | `/api/v1/friends/search` | JWT required | Query `q` | `List<FriendResponse>` | `200` |
| `DELETE` | `/api/v1/friends/{userId}` | JWT required | Path `userId` | — | `204 / 400` |
| `GET` | `/api/v1/friends/{userId}/profile` | JWT required | Path `userId` | `FriendProfileResponse` (`rankProgress`, `comparison`) | `200 / 400 / 404` |

## Data Model

| Table | Entity / owner | Key columns | Constraints / source |
|---|---|---|---|
| `friend_requests` | `friendship/adapter/out/persistence/FriendRequestEntity.java` | `id`, `sender_id`, `receiver_id`, `status`, `created_at`, `updated_at` | `chk_not_self_request`, indexes on `sender_id`, `receiver_id`, `status`; created by `src/main/resources/db/migration/V7__create_friend_request_table.sql` |
| `friendships` | `friendship/adapter/out/persistence/FriendshipEntity.java` | `id`, `user_id`, `friend_id`, `created_at` | `uq_user_friend`, `chk_not_self_friend`; two rows per friendship by design; created by `src/main/resources/db/migration/V8__create_friendship_table.sql` |
| `follows` | `friendship/adapter/out/persistence/FollowEntity.java` + `FollowEntityId.java` | `follower_id`, `following_id`, `created_at` | Composite PK and `chk_not_self_follow` exist only in `supabase/migrations/20260412183914_remote_schema.sql`; there is **no** matching Flyway migration under `src/main/resources/db/migration/` |

## Domain Logic

`friendship/application/usecase/FriendshipUseCaseService.java` is the feature orchestrator. `sendFriendRequest()` validates self/duplicate/already-friends conditions, persists a `PENDING` request, and enriches the response with sender/receiver profile data. `acceptFriendRequest()` updates the request to `ACCEPTED`, creates the two directional friendship rows, then triggers achievement reevaluation for both users. `getFriendProfile()` is friend-gated and combines user identity, rank progress from `ranking/`, and a stat-by-stat comparison assembled from the five current lifestat repositories.

## Events

None. This feature calls `achievement/application/usecase/AchievementUnlockUseCaseService.java` synchronously after request acceptance instead of publishing a Spring `ApplicationEvent`.

## Dependencies

This feature depends on `user/` for identity lookup, `ranking/` for rank progress, `lifestats/` for stat comparison, and `achievement/` for post-accept unlock checks. `leaderboard/`, `search/`, `feed/`, and friend-only achievement views all depend on `friendship/` relationships.

## Key Files

| File | Role |
|---|---|
| `friendship/adapter/in/rest/FriendController.java` | `/api/v1/friends/**` REST surface |
| `friendship/application/usecase/FriendshipUseCaseService.java` | Send/accept/reject/list/remove/profile-comparison logic |
| `friendship/application/port/{FriendRequestRepository,FriendshipRepository,FollowRepository}.java` | Hexagonal outbound ports |
| `friendship/adapter/out/persistence/{FriendRequestEntity,FriendshipEntity,FollowEntity}.java` | Table mappings |
| `friendship/adapter/out/persistence/{FriendRequestJpaRepository,FriendshipJpaRepository,FollowJpaRepository}.java` | JPA query layer |
| `friendship/domain/model/{FriendRequest,FriendRequestStatus,Friendship,Follow,FollowId}.java` | Core domain objects |
| `friendship/adapter/in/rest/dto/{FriendRequestResponse,FriendResponse,FriendProfileResponse,StatComparisonResponse}.java` | Response payloads |

## Known Limitations

- `Follow` is not user-facing. `friendship/domain/model/Follow.java`, `friendship/adapter/out/persistence/FollowEntity.java`, and `friendship/adapter/out/persistence/FollowRepositoryAdapter.java` exist, but `friendship/application/port/FollowRepository.java` exposes only read-side count/existence methods, there is no save/delete port, `friendship/application/usecase/` contains no `Follow` logic, and `friendship/adapter/in/rest/` exposes no follow/unfollow endpoint.
- The `follows` table is outside the repo's Flyway history: it exists in `supabase/migrations/20260412183914_remote_schema.sql`, but no `src/main/resources/db/migration/V*.sql` file creates it.

## Future Work

- `product-backlog.md` Epic 4.4 still scopes follow/unfollow as future work; the current codebase only has the persistence-side placeholder.
- `product-backlog.md` Epic 6.3 scopes follow notifications, which depends on follow/unfollow becoming a real application flow first.
