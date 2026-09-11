# S5-02 — Friends Leaderboard: Service + Endpoint

> **Status**: `NOT_STARTED`
> **Priority**: 🔴 Must
> **Depends on**: S5-01

## Goal

Implement `GET /api/v1/leaderboard` — paginated friends leaderboard ranked by total score. Includes the current user in the list.

## Tasks

### Service
- [ ] Create `LeaderboardService`
- [ ] Implement `getFriendsLeaderboard(UUID userId, Pageable pageable)`:
  1. Get friend IDs via `FriendshipRepository.findFriendIdsByUserId()`
  2. Add current user's ID to the list
  3. Fetch all users by IDs, sort by `totalScore` DESC
  4. Build `LeaderboardEntryResponse` list with position numbers
  5. Mark `isCurrentUser` flag
  6. Return wrapped `LeaderboardResponse` with metadata (totalFriends, currentUserPosition, currentUserScore)

### Controller
- [ ] Create `LeaderboardController` at `/api/v1/leaderboard`
- [ ] `GET /api/v1/leaderboard` endpoint with `@RequestParam page`, `size`
- [ ] OpenAPI annotations (`@Tag`, `@Operation`, `@ApiResponses`)

### Tests
- [ ] `LeaderboardServiceTest` — unit tests for friends leaderboard
- [ ] `LeaderboardControllerTest` — WebMvc tests for endpoint

## Related Skills
- `conventions` — controller/service patterns
- `api-reference` — endpoint documentation
