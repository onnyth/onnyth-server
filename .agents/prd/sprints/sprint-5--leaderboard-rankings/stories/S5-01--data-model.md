# S5-01 — Data Model: LeaderboardSnapshot + DTOs + Repo Additions

> **Status**: `NOT_STARTED`
> **Priority**: 🔴 Must
> **Depends on**: Sprint 4 (friends system entities)

## Goal

Create the `LeaderboardSnapshot` entity, migration, DTOs for all leaderboard responses, and add required repository methods to `FriendshipRepository` and `LifeStatRepository`.

## Tasks

### Entity + Migration
- [ ] Create `LeaderboardSnapshot` entity (`leaderboard_snapshots` table): id, userId, friendOwnerId, position, score, snapshotDate (LocalDate), category (StatCategory, nullable)
- [ ] Create `V9__create_leaderboard_snapshots_table.sql` with index on `(user_id, friend_owner_id, snapshot_date)`

### DTOs
- [ ] Create `LeaderboardEntryResponse` record: position, userId, username, fullName, profilePic, totalScore, rankTier, isCurrentUser, positionChange, isNew
- [ ] Create `LeaderboardResponse` record: entries (list), totalFriends, currentUserPosition, currentUserScore
- [ ] Create `UserLeaderboardPositionResponse` record: position, totalParticipants, score, pointsToNextPosition, userAheadUsername, userAheadId
- [ ] Create `CategoryLeaderboardEntryResponse` record: position, userId, username, fullName, profilePic, categoryValue, category, rankTier, isCurrentUser

### Repository Additions
- [ ] Add `findFriendIdsByUserId(UUID userId)` → `List<UUID>` to `FriendshipRepository` (JPQL: `SELECT f.friendId FROM Friendship f WHERE f.userId = :userId`)
- [ ] Create `LeaderboardSnapshotRepository` with methods:
  - `findByFriendOwnerIdAndSnapshotDate(UUID, LocalDate)` → `List<LeaderboardSnapshot>`
  - `findByFriendOwnerIdAndSnapshotDateAndCategory(UUID, LocalDate, StatCategory)` → `List<LeaderboardSnapshot>`
- [ ] Add `findAllByUserIdInAndCategory(List<UUID>, StatCategory)` → `List<LifeStat>` to `LifeStatRepository`

## Related Skills
- `data-layer` — entity/migration patterns
- `conventions` — DTO pattern
