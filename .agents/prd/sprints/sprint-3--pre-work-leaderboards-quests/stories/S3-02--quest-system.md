# S3-02 — Quest System

> **Status**: `DONE`
> **Priority**: 🔴 Must
> **Sprint**: Sprint 3
> **Blocks Client Stories**: S-006 (Quests placeholder → future real tab)

## What & Why

Build the quest backend so the client can switch from "Coming Soon" placeholders to a real quests system. Quests are challenges users can complete to earn XP (added to total score), driving engagement and gamification.

## API Endpoints (from Brief §1)

| Method | Endpoint | Request Body | Response Body | Notes |
|---|---|---|---|---|
| GET | `/api/v1/quests/active` | — | `QuestListResponse` | List active quests for current user |
| GET | `/api/v1/quests/{id}` | — | `QuestResponse` | Get single quest details |
| POST | `/api/v1/quests/{id}/complete` | — | `QuestCompletionResponse` | Mark quest as completed, award XP |

## Technical Tasks

### Data Model (from Brief §2)
- [ ] Create `Quest.java` entity — `id` (UUID), `title`, `description`, `xpReward` (int), `category` (StatCategory), `status` (QuestStatus enum), `deadline` (Instant), `createdAt`
- [ ] Create `QuestStatus.java` enum — `ACTIVE`, `EXPIRED`, `ARCHIVED`
- [ ] Create `QuestCompletion.java` entity — `id` (UUID), `userId` (UUID), `questId` (UUID), `completedAt` (Instant)
- [ ] Unique constraint on `QuestCompletion(userId, questId)` — prevent double completion
- [ ] Create Flyway migration: `V6__create_quests_tables.sql`

### Repositories
- [ ] Create `QuestRepository.java` — `findAllByStatus(QuestStatus)`, `findByIdAndStatus(UUID, QuestStatus)`
- [ ] Create `QuestCompletionRepository.java` — `findAllByUserId(UUID)`, `existsByUserIdAndQuestId(UUID, UUID)`

### Services (from Brief §3)
- [ ] Create `QuestService.java`:
  - `getActiveQuests(UUID userId)` — returns active quests with user's completion status
  - `getQuestById(UUID questId)` — single quest details
  - `completeQuest(UUID userId, UUID questId)` — validate, record completion, award XP to user's total score
- [ ] XP award: add `xpReward` to user's `totalScore` → trigger rank recalculation via existing event system

### Controllers & DTOs
- [ ] Create `QuestResponse.java` — `id`, `title`, `description`, `xpReward`, `category`, `status`, `deadline`, `completed` (boolean for current user)
- [ ] Create `QuestListResponse.java` — `quests: List<QuestResponse>`, `completedCount`, `totalCount`
- [ ] Create `QuestCompletionResponse.java` — `questId`, `xpAwarded`, `newTotalScore`, `rankTier`
- [ ] Create `QuestController.java` at `/api/v1/quests`

### Exceptions
- [ ] Create `QuestNotFoundException.java` — 404
- [ ] Create `QuestAlreadyCompletedException.java` — 409
- [ ] Create `QuestExpiredException.java` — 400

### Tests
- [ ] Unit test: `QuestServiceTest.java`
- [ ] Controller test: `QuestControllerTest.java`

## Related Skills

- `conventions` — coding patterns
- `data-layer` — entity/migration patterns (next migration: V6)
- `scoring-and-ranking` — XP awards trigger score recalculation via `StatChangedEvent`
- `error-handling` — exception pattern
- `testing` — test structure

## Skill Updates Required

- [ ] `data-layer/SKILL.md` — Added Quest, QuestCompletion entities, QuestStatus enum, 2 repositories, V6 migration
- [ ] `api-reference/SKILL.md` — Added 3 quest endpoints
- [ ] `error-handling/SKILL.md` — Added QuestNotFoundException, QuestAlreadyCompletedException, QuestExpiredException
- [ ] `testing/SKILL.md` — Added QuestServiceTest, QuestControllerTest
- [ ] `skills/SKILL.md` — Consider creating new `quests/SKILL.md` skill if complexity warrants it

## Notes

- Quests are **system-created** for now (seeded via migration or admin) — no user-facing create/edit endpoints yet
- XP from quests adds directly to `totalScore`, so rank tiers automatically adjust
- `category` on Quest maps to `StatCategory` — allows category-specific quests (e.g., "Complete a Fitness quest")
- Deadline is optional — quests without deadlines are evergreen
