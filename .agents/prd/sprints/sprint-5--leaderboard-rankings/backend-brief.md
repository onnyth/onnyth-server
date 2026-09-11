# Backend Sprint Brief — Sprint 3: Leaderboard & Rankings

> **Sprint**: Sprint 3
> **Source PRD**: PRD-003
> **Date**: 2026-03-03

---

## 1. API Endpoints Required

| Method | Endpoint | Client Story | Priority |
|---|---|---|---|
| GET | `/api/v1/leaderboard?page={page}&size={size}&category={category}` | S-018, S-021 | 🔴 Blocking |
| GET | `/api/v1/leaderboard/my-position` | S-020 | 🔴 Blocking |

---

## 2. Data Model Changes

### New Entities

| Entity | Fields | Notes |
|---|---|---|
| `LeaderboardSnapshot` | id (UUID), userId, friendOwnerId, position, score, snapshotDate (LocalDate), category (StatCategory, nullable) | Weekly snapshot for rank change tracking |

### New DTOs

| DTO | Fields |
|---|---|
| `LeaderboardEntryResponse` | position, userId, username, fullName, profilePic, totalScore, rankTier, isCurrentUser, positionChange, isNew |
| `LeaderboardResponse` | entries (list), totalFriends, currentUserPosition, currentUserScore |
| `UserLeaderboardPositionResponse` | position, totalParticipants, score, pointsToNextPosition, userAheadUsername, userAheadId |
| `CategoryLeaderboardEntryResponse` | position, userId, username, fullName, profilePic, categoryValue, category, rankTier, isCurrentUser |

### Database Migrations
- `V8__create_leaderboard_snapshots_table.sql` — leaderboard_snapshots table with index on (user_id, friend_owner_id, snapshot_date)

### Repository Changes
- Add `findFriendIdsByUserId()` to `FriendshipRepository` (if not already present from Sprint 2)
- Create `LeaderboardSnapshotRepository` with snapshot queries
- Add `findByUserIdsAndCategory()` to `LifeStatRepository`

---

## 3. Business Logic / Services

| Service | Methods | Key Logic |
|---|---|---|
| `LeaderboardService` | `getFriendsLeaderboard(userId, pageable)` | Join friendships with users, order by totalScore DESC, include currentUser, paginate |
| | `getLeaderboardByCategory(userId, category, pageable)` | Same but ordered by specific stat's value; users without that stat ranked last |
| | `getUserPosition(userId)` | Calculate position among friends, find user ahead, compute points gap |
| `LeaderboardSnapshotService` | `takeWeeklySnapshot()` | `@Scheduled(cron = "0 0 0 * * SUN")` — save all positions for all users |
| | `getPositionChanges(friendOwnerId, userIds)` | Compare current positions vs last week's snapshot |

### Application Config
- Add `@EnableScheduling` to `OnnythServerApplication.java`

---

## 4. Client → Backend Story Mapping

| Client Story | Backend Dependencies | Can Parallel? |
|---|---|---|
| S-017 (Types/Service/Store) | Both endpoints (mock mode) | ✅ Yes — mock mode |
| S-018 (Leaderboard Screen) | `GET /leaderboard` | ✅ Yes — mock data |
| S-019 (Podium Design) | None (pure UI) | ✅ Yes |
| S-020 (Position Card) | `GET /leaderboard/my-position` | ✅ Yes — mock mode |
| S-021 (Category Filter) | `GET /leaderboard?category=X` | ✅ Yes — mock mode |
| S-022 (Rank Changes) | Snapshot data in response | ✅ Yes — mock positionChange |
| S-023 (Home Widget) | `GET /leaderboard` (partial) | ✅ Yes — mock mode |

---

## 5. Suggested Backend Sprint Ordering

| Order | Backend Work | Blocks Client Story |
|---|---|---|
| 1 | `LeaderboardEntryResponse` + `LeaderboardResponse` DTOs | All leaderboard features |
| 2 | FriendshipRepository: `findFriendIdsByUserId()` | All (if not from Sprint 2) |
| 3 | `LeaderboardService.getFriendsLeaderboard()` + `GET /leaderboard` | S-018 (real mode) |
| 4 | `UserLeaderboardPositionResponse` + `getUserPosition()` + `GET /my-position` | S-020 (real mode) |
| 5 | `LifeStatRepository.findByUserIdsAndCategory()` + `getLeaderboardByCategory()` | S-021 (real mode) |
| 6 | `LeaderboardSnapshot` entity + migration + repository | S-022 (real mode) |
| 7 | `LeaderboardSnapshotService` + `@Scheduled` + `@EnableScheduling` | S-022 (real mode) |
| 8 | Integrate positionChange/isNew into `LeaderboardEntryResponse` | S-022 (real mode) |

---

## 6. Notes

- **All client stories can proceed in parallel** — full mock mode support.
- Backend should prioritize orders 1–4 (core leaderboard + position) to enable real API integration.
- The weekly snapshot job (orders 6–8) is lower priority — rank changes work fine with mock data until snapshots are live.
- The `category` query parameter on `GET /leaderboard` is optional — omitting it returns the overall leaderboard.
