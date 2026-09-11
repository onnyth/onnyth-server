# Backend Sprint Brief — Sprint 001

> **Source PRD**: PRD-001 — Bottom Tab Navigation & Core Screen Scaffolding
> **Client Sprint**: sprint-001
> **Date**: 2026-03-03

---

## 1. API Endpoints Required

| Client Story | Method | Endpoint | Request Body | Response Body | Notes |
|---|---|---|---|---|---|
| S-004 (Home Tab) | GET | `/api/v1/profile/card` | — | `ProfileCardResponse` | **Already exists** — no backend change |
| S-005 (Profile Tab) | GET | `/api/v1/stats` | — | `LifeStatResponse[]` | **Already exists** — no backend change |
| S-005 (Profile Tab) | PUT | `/api/v1/stats/{category}` | `StatUpdateRequest` | `StatUpdateResponse` | **Already exists** — no backend change |
| S-006 (Leaderboards placeholder) | GET | `/api/v1/leaderboards` | `?type=global&limit=50` | `LeaderboardResponse` | **Future** — not needed for placeholder |
| S-006 (Quests placeholder) | GET | `/api/v1/quests/active` | — | `QuestListResponse` | **Future** — not needed for placeholder |
| S-006 (Search placeholder) | GET | `/api/v1/users/search` | `?q=string` | `UserSearchResponse` | **Future** — not needed for placeholder |

---

## 2. Data Model Changes

| Entity | Change Type | Details |
|---|---|---|
| — | — | **No data model changes required for Sprint 001** |

> The bottom tab navigation feature is a pure client-side UI restructuring. All existing API endpoints and data models remain unchanged.

### Future Data Models (for backend to pre-plan)

| Entity | Change Type | Needed For | Details |
|---|---|---|---|
| `Quest` | NEW | Quests tab (future sprint) | `id`, `title`, `description`, `xpReward`, `status`, `category`, `deadline`, `createdAt` |
| `QuestCompletion` | NEW | Quests tab (future sprint) | `id`, `userId`, `questId`, `completedAt` |
| `Leaderboard` (view/materialized) | NEW | Leaderboards tab (future sprint) | Aggregated scores by user, sortable by total score, filterable by period |
| `UserFollow` | NEW | Search/Social (future sprint) | `followerId`, `followeeId`, `createdAt` |

---

## 3. Business Logic / Services

| Service | Purpose | Triggered By |
|---|---|---|
| — | **No new business logic required for Sprint 001** | — |

### Future Services (for backend to pre-plan)

| Service | Purpose | Triggered By |
|---|---|---|
| `LeaderboardService` | Calculate and cache global/friends leaderboards | Leaderboards tab (future) |
| `QuestService` | Create, assign, track, complete quests; award XP | Quests tab (future) |
| `UserSearchService` | Full-text search across user profiles | Search tab (future) |
| `FollowService` | Follow/unfollow users, build social graph | Search/Social (future) |

---

## 4. Client → Backend Story Mapping

| Client Story | Backend Work Required | Backend Priority |
|---|---|---|
| S-001: Restructure Tab Navigator | **None** (pure client routing) | — |
| S-002: Custom Tab Bar Component | **None** (pure client UI) | — |
| S-003: Tab Icons and Labels | **None** (pure client UI) | — |
| S-004: Move Dashboard → Home Tab | **None** — uses existing `GET /api/v1/profile/card` | — |
| S-005: Profile Screen as Tab | **None** — uses existing `GET /api/v1/stats`, `PUT /api/v1/stats/{category}` | — |
| S-006: Placeholder Screens | **None** — screens show "Coming Soon" (no API calls) | — |
| S-007: Update Route Guards | **None** (pure client routing) | — |

> **Summary**: Sprint 001 is **100% client-side**. No backend work is blocking. All existing endpoints continue to work as-is.

---

## 5. Suggested Backend Sprint Ordering

| Order | Backend Task | Blocks Client Story | Timeline |
|---|---|---|---|
| — | **No blocking backend work for Sprint 001** | — | — |

### Recommended Pre-Work for Future Sprints

The following backend work should be **started in parallel** with client Sprint 001 to unblock future client sprints:

| Priority | Backend Task | Unblocks | Estimated Effort |
|---|---|---|---|
| 🔴 High | Design and implement `GET /api/v1/leaderboards` endpoint + `LeaderboardService` | Leaderboards tab (future Sprint 2–3) | M |
| 🔴 High | Design and implement Quest CRUD + `GET /api/v1/quests/active` endpoint | Quests tab (future Sprint 2–3) | L |
| 🟡 Medium | Design and implement `GET /api/v1/users/search` with full-text search | Search tab (future Sprint 3–4) | M |
| 🟢 Low | Design follow/unfollow system (`UserFollow` entity) | Social features (future) | S |

---

## 6. Notes for Backend Agent

- **Sprint 001 requires zero backend changes** — the client team is restructuring navigation and UI only. All existing endpoints (`/api/v1/profile/card`, `/api/v1/stats`, `/api/v1/stats/{category}`) remain unchanged.
- **Use this sprint's downtime** to start designing the Leaderboard and Quest data models / services. These will be needed as soon as the client team moves past placeholders.
- The client currently uses `USE_MOCK_AUTH = true` for all API calls. When the real backend endpoints for leaderboards, quests, and search are ready, the client team will switch to real calls using the same `api.ts` interceptor pattern.
- **Existing API reference** for the client's current endpoints: see `onnyth-client/resources/api-reference.md`.
