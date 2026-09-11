# Backend Sprint Brief — Sprint 2: Friends System

> **Sprint**: Sprint 2
> **Source PRD**: PRD-002
> **Date**: 2026-03-03

---

## 1. API Endpoints Required

| Method | Endpoint | Client Story | Priority |
|---|---|---|---|
| GET | `/api/v1/users/search?q={query}&page={page}&size={size}` | S-010, S-011 | 🔴 Blocking |
| POST | `/api/v1/friends/request/{userId}` | S-011 | 🔴 Blocking |
| GET | `/api/v1/friends/requests/received` | S-012 | 🔴 Blocking |
| GET | `/api/v1/friends/requests/sent` | S-012 | 🔴 Blocking |
| GET | `/api/v1/friends/requests/count` | S-016 | 🟡 Medium |
| PUT | `/api/v1/friends/request/{requestId}/accept` | S-012 | 🔴 Blocking |
| PUT | `/api/v1/friends/request/{requestId}/reject` | S-012 | 🔴 Blocking |
| GET | `/api/v1/friends?sort={sort}&page={page}&size={size}` | S-013 | 🔴 Blocking |
| GET | `/api/v1/friends/search?q={query}` | S-013 | 🟡 Medium |
| DELETE | `/api/v1/friends/{userId}` | S-014 | 🔴 Blocking |
| GET | `/api/v1/friends/{userId}/profile` | S-015 | 🔴 Blocking |

---

## 2. Data Model Changes

### New Entities

| Entity | Fields | Notes |
|---|---|---|
| `FriendRequest` | id (UUID), senderId (UUID), receiverId (UUID), status (enum), createdAt, updatedAt | Status: PENDING, ACCEPTED, REJECTED |
| `FriendRequestStatus` | PENDING, ACCEPTED, REJECTED | Enum |
| `Friendship` | id (UUID), userId (UUID), friendId (UUID), createdAt | Bidirectional: 2 rows per friendship |

### New DTOs

| DTO | Fields |
|---|---|
| `UserSearchResponse` | userId, username, fullName, profilePic, rankTier, isFriend, requestPending |
| `FriendRequestDTO` | requestId, senderId, receiverId, status, createdAt |
| `FriendRequestResponse` | requestId, sender (UserSearchResponse), createdAt |
| `FriendResponse` | userId, username, fullName, profilePic, rankTier, totalScore, friendSince |
| `FriendProfileResponse` | userId, username, fullName, profilePic, rankTier, totalScore, rankProgress, stats[], comparison |
| `StatComparisonResponse` | scoreDifference, higherIn, lowerIn |

### Database Migrations
- `V6__create_friend_request_table.sql` — friend_requests table
- `V7__create_friendship_table.sql` — friendships table with unique constraint on (userId, friendId)

---

## 3. Business Logic / Services

| Service | Methods | Key Logic |
|---|---|---|
| `UserSearchService` | `searchUsers(query, currentUserId, pageable)` | Case-insensitive partial match, exclude self, annotate friendship status |
| `FriendshipService` | `sendFriendRequest(senderId, receiverId)` | Validate: not self, not already friends, no pending request |
| | `acceptFriendRequest(requestId, currentUserId)` | Validate: only receiver can accept, create 2 Friendship rows |
| | `rejectFriendRequest(requestId, currentUserId)` | Validate: only receiver can reject, set status=REJECTED |
| | `getReceivedRequests(userId)` | Pending requests where receiverId=userId |
| | `getSentRequests(userId)` | Pending requests where senderId=userId |
| | `getPendingRequestCount(userId)` | Count of received pending |
| | `getFriends(userId, pageable, sort)` | Join friendships with users, return sorted |
| | `searchFriends(userId, query)` | Filter friends by username match |
| | `removeFriend(userId, friendId)` | Delete both Friendship rows |
| | `getFriendProfile(userId, friendId)` | Full profile with stats + comparison |

### Custom Exceptions
- `DuplicateFriendRequestException`
- `AlreadyFriendsException`
- `FriendRequestNotFoundException`
- `UnauthorizedFriendRequestActionException`
- `NotFriendsException`

---

## 4. Client → Backend Story Mapping

| Client Story | Backend Dependencies | Can Parallel? |
|---|---|---|
| S-008 (Types/Service/Store) | All endpoints (mock mode) | ✅ Yes — mock mode |
| S-010 (Search Screen) | `GET /users/search` | ✅ Yes — mock data |
| S-011 (Send Request) | `POST /friends/request/{userId}` | ✅ Yes — mock mode |
| S-012 (Requests View) | `GET /requests/received`, `GET /requests/sent`, `PUT /accept`, `PUT /reject` | ✅ Yes — mock mode |
| S-013 (Friends List) | `GET /friends`, `GET /friends/search` | ✅ Yes — mock mode |
| S-014 (Remove Friend) | `DELETE /friends/{userId}` | ✅ Yes — mock mode |
| S-015 (Friend Profile) | `GET /friends/{userId}/profile` | ✅ Yes — mock mode |
| S-016 (Badge) | `GET /requests/count` | ✅ Yes — mock mode |

---

## 5. Suggested Backend Sprint Ordering

| Order | Backend Work | Blocks Client Story |
|---|---|---|
| 1 | FriendRequest entity + FriendRequestStatus enum + migrations | All friend operations |
| 2 | Friendship entity + migration | Accept, friends list, remove |
| 3 | UserSearchService + `GET /users/search` | S-010 (real search) |
| 4 | FriendshipService (send/accept/reject) + endpoints | S-011, S-012 |
| 5 | FriendshipService (getFriends, searchFriends) + endpoints | S-013 |
| 6 | FriendshipService (removeFriend) + endpoint | S-014 |
| 7 | FriendshipService (getFriendProfile + comparison) + endpoint | S-015 |
| 8 | getPendingRequestCount + endpoint | S-016 |

---

## 6. Notes

- **All client stories can proceed in parallel** — the frontend uses full mock mode (`USE_MOCK_AUTH`) so no backend endpoints are blocking Sprint 2 frontend execution.
- Backend team should prioritize orders 1–4 (entities + core friend operations) to unblock real API integration in Sprint 3+.
- The `StatComparisonResponse` computation (higherIn/lowerIn categories) should be done server-side to avoid exposing raw user stats to non-friends.
