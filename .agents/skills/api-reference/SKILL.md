---
name: api-reference
description: Complete REST API reference — all endpoints, methods, authentication, request/response DTOs
---

# API Reference

Base URL: `/api`  
API version prefix: `/api/v1`

## Authentication Endpoints

**Base**: `/api/v1/auth` — **Public** (no JWT required)

| Method | Path | Description | Request Body | Response | Status |
|---|---|---|---|---|---|
| `POST` | `/signup` | Register new user | `AuthRequest` | `SignupResponse` | 201 |
| `POST` | `/login` | Login & get tokens | `AuthRequest` | `LoginResponse` | 200 |
| `POST` | `/refresh` | Refresh access token | `RefreshTokenRequest` | `RefreshTokenResponse` | 200 |
| `POST` | `/logout` | Invalidate session | — (Authorization header) | — | 204 |

---

## Profile Endpoints

**Base**: `/api/v1/profile` — **Authenticated** (JWT required)

| Method | Path | Description | Request Body | Response | Status |
|---|---|---|---|---|---|
| `GET` | `/` | Get own profile | — | `ProfileResponse` | 200 |
| `PUT` | `/` | Update own profile | `ProfileUpdateRequest` | `ProfileResponse` | 200 |
| `GET` | `/check-username/{username}` | Check username availability | — | `{ username, available }` | 200 |
| `POST` | `/picture` | Upload profile picture | `multipart/form-data` (`file`) | `ProfileResponse` | 200 |
| `GET` | `/card` | Get own profile card | — | `ProfileCardResponse` | 200 |
| `GET` | `/rank` | Get rank progress | — | `RankProgressResponse` | 200 |

---

## Life Stats Endpoints

**Base**: `/api/v1/stats` — **Authenticated** (JWT required)

| Method | Path | Description | Request Body | Response | Status |
|---|---|---|---|---|---|
| `POST` | `/` | Save single stat | `StatInputRequest` | `LifeStatResponse` | 201 |
| `POST` | `/bulk` | Save multiple stats | `BulkStatInputRequest` | `List<LifeStatResponse>` | 201 |
| `GET` | `/` | Get all user stats | — | `List<LifeStatResponse>` | 200 |
| `PUT` | `/{category}` | Update existing stat | `StatUpdateRequest` | `StatUpdateResponse` | 200 |

`{category}` is one of: `CAREER`, `WEALTH`, `FITNESS`, `EDUCATION`, `SOCIAL_INFLUENCE`

---

## Leaderboard Endpoints

**Base**: `/api/v1/leaderboard` — **Authenticated** (JWT required)

| Method | Path | Description | Query Params | Response | Status |
|---|---|---|---|---|---|
| `GET` | `/` | Friends leaderboard (overall or by category) | `page`, `size`, `category` (optional) | `LeaderboardResponse` or `Page<CategoryLeaderboardEntryResponse>` | 200 |
| `GET` | `/my-position` | Current user's position among friends | — | `UserLeaderboardPositionResponse` | 200 |

---

## Achievement Endpoints

**Base**: `/api/v1/achievements` — **Authenticated** (JWT required)

| Method | Path | Description | Response | Status |
|---|---|---|---|---|
| `GET` | `/` | All achievements with unlock status + progress | `List<AchievementResponse>` | 200 |
| `GET` | `/category/{category}` | Achievements by category | `List<AchievementResponse>` | 200 |
| `GET` | `/unlocked` | Only unlocked achievements | `List<AchievementResponse>` | 200 |
| `GET` | `/stats` | Achievement stats (counts + points) | `AchievementStatsResponse` | 200 |
| `GET` | `/displayed` | Displayed badge slots (up to 3) | `List<DisplayedBadgeResponse>` | 200 |
| `PUT` | `/displayed` | Update displayed badges | `List<DisplayedBadgeResponse>` | 200 |
| `GET` | `/user/{friendId}` | Friend's unlocked achievements | `List<AchievementResponse>` | 200 |
| `GET` | `/user/{friendId}/stats` | Friend's achievement stats | `AchievementStatsResponse` | 200 |

`{category}` is one of: `STATS`, `SOCIAL`, `STREAK`, `MILESTONE`, `SPECIAL`

---

## Quest Endpoints


**Base**: `/api/v1/quests` — **Authenticated** (JWT required)

| Method | Path | Description | Request Body | Response | Status |
|---|---|---|---|---|---|
| `GET` | `/active` | List active quests | — | `QuestListResponse` | 200 |
| `GET` | `/{id}` | Get quest details | — | `QuestResponse` | 200 |
| `POST` | `/{id}/complete` | Complete a quest | — | `QuestCompletionResponse` | 200 |

---

## Bookmark Endpoints

**Base**: `/api/v1/bookmarks` — **Authenticated** (JWT required; not user-scoped — see
`.agents/skills/bookmark/SKILL.md` and `docs/known-issues.md`)

| Method | Path | Description | Request Body | Response | Status |
|---|---|---|---|---|---|
| `POST` | `/` | Create bookmark (requires `Idempotency-Key` header) | `CreateBookmarkRequest` | `CreateBookmarkResponse` | 201 |
| `GET` | `/{id}` | Get bookmark by ID | — | `CreateBookmarkResponse` | 200 |
| `GET` | `/` | List bookmarks, paginated, optional `tag` filter | — | `BookmarkPageResponse` | 200 |
| `PUT` | `/{id}` | Update bookmark | `BookmarkUpdateRequest` | `CreateBookmarkResponse` | 200 |
| `DELETE` | `/{id}` | Delete bookmark | — | — | 204 |

Full detail (idempotency flow, Kafka event, error cases): `.agents/skills/bookmark/SKILL.md`.

---

**Base**: `/api` — Mixed authentication

| Method | Path | Auth | Description | Response | Status |
|---|---|---|---|---|---|
| `GET` | `/users` | Required | List all users | `List<User>` | 200 |
| `GET` | `/users/{id}` | Required | Get user by ID | `User` | 200 |
| `GET` | `/users/email/{email}` | Required | Get user by email | `User` | 200 |
| `POST` | `/users` | Required | Create user | `User` | 200 |
| `PUT` | `/users/{id}` | Required | Update user | `User` | 200 |
| `DELETE` | `/users/{id}` | Required | Delete user | — | 204 |
| `GET` | `/v1/users/{userId}/card` | **Public** | Get any user's profile card | `ProfileCardResponse` | 200 |

---

## Request DTOs

### AuthRequest
```json
{ "email": "user@example.com", "password": "secret123" }
```

### ProfileUpdateRequest
```json
{ "username": "john_doe", "fullName": "John Doe", "profilePic": "https://..." }
```
All fields optional. Validation: username 3-20 chars `[a-zA-Z0-9_]`, fullName max 100 chars.

### StatInputRequest
```json
{ "category": "CAREER", "value": 75, "metadata": "optional notes" }
```
`category` required, `value` required (1-100), `metadata` optional.

### BulkStatInputRequest
```json
{ "stats": [ { "category": "CAREER", "value": 75 }, { "category": "WEALTH", "value": 60 } ] }
```

### StatUpdateRequest
```json
{ "newValue": 80, "reason": "Got a promotion" }
```
`newValue` required (1-100), `reason` optional.

### RefreshTokenRequest
```json
{ "refreshToken": "..." }
```

### CreateBookmarkRequest / BookmarkUpdateRequest
```json
{ "url": "https://example.com", "title": "Example", "tags": ["reading", "tech"] }
```
`url` required (valid URI), `title` required (max 255 chars), `tags` optional (each max 50 chars).
`POST` additionally requires an `Idempotency-Key` header (not part of the body).

---

## Response DTOs

### SignupResponse
```json
{ "message": "Please check your email to confirm your account", "email": "user@example.com" }
```

### LoginResponse
```json
{
    "accessToken": "eyJ...",
    "refreshToken": "...",
    "expiresAt": 1709424000,
    "user": { "id": "uuid", "email": "...", "username": "...", "fullName": "...", "profilePic": "..." }
}
```

### ProfileResponse
```json
{
    "id": "uuid", "email": "...", "username": "...", "fullName": "...",
    "profilePic": "...", "emailVerified": true, "profileComplete": true,
    "totalScore": 450, "createdAt": "...", "updatedAt": "..."
}
```

### ProfileCardResponse
```json
{
    "userId": "uuid", "username": "...", "fullName": "...", "profilePic": "...",
    "totalScore": 450, "rankTier": "Gold", "rankBadgeUrl": "🥇"
}
```

### RankProgressResponse
```json
{
    "currentTier": "Gold", "currentBadge": "🥇", "currentScore": 450,
    "nextTier": "Platinum", "nextBadge": "💎",
    "pointsToNextTier": 50, "progressPercent": 80.0
}
```

### LifeStatResponse
```json
{
    "category": "CAREER", "displayName": "Career", "value": 75,
    "pointsContributed": 75, "lastUpdated": "...", "metadata": "..."
}
```

### StatUpdateResponse
```json
{
    "category": "CAREER", "displayName": "Career",
    "previousValue": 70, "newValue": 80, "totalScore": 460, "scoreChange": 12
}
```

### LeaderboardResponse (Friends)
```json
{
    "entries": [
        { "position": 1, "userId": "uuid", "username": "...", "fullName": "...",
          "profilePic": "...", "totalScore": 500, "rankTier": "Platinum",
          "isCurrentUser": false, "positionChange": 2, "isNew": false }
    ],
    "totalFriends": 5,
    "currentUserPosition": 3,
    "currentUserScore": 450
}
```

### UserLeaderboardPositionResponse
```json
{
    "position": 3, "totalParticipants": 6, "score": 450,
    "pointsToNextPosition": 50, "userAheadUsername": "alice", "userAheadId": "uuid"
}
```

### CategoryLeaderboardEntryResponse
```json
{
    "position": 1, "userId": "uuid", "username": "...", "fullName": "...",
    "profilePic": "...", "categoryValue": 80, "category": "Fitness",
    "rankTier": "Gold", "isCurrentUser": true
}
```

### QuestResponse
```json
{
    "id": "uuid", "title": "...", "description": "...",
    "xpReward": 50, "category": "FITNESS", "status": "ACTIVE",
    "deadline": "...", "completed": false
}
```

### QuestListResponse
```json
{ "quests": [QuestResponse], "completedCount": 1, "totalCount": 5 }
```

### QuestCompletionResponse
```json
{
    "questId": "uuid", "questTitle": "...",
    "xpAwarded": 100, "newTotalScore": 500, "rankTier": "Platinum"
}
```

### CreateBookmarkResponse
```json
{
    "id": "uuid", "url": "https://example.com", "title": "Example",
    "tags": ["reading", "tech"], "createdAt": "...", "updatedAt": "..."
}
```

### BookmarkPageResponse
Standard paginated wrapper around a page of `CreateBookmarkResponse` entries (see
`BookmarkPageResponse.fromPage(Page<Bookmark>)`).

---

## Swagger UI

Available at: `/swagger-ui.html` (when `springdoc.swagger-ui.enabled=true`)  
API docs at: `/api-docs`
