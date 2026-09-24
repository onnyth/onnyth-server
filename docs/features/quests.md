# Quests

## Status

Implemented

## Purpose

This feature exposes the quest catalog and one-time quest completion flow. Quests are system-created challenges tied to a stat-domain category and reward the user by increasing total score, which can in turn affect rank and friend leaderboards.

## User Capabilities

- View all currently active quests, including which ones they have already completed.
- Fetch one quest by id.
- Complete an active quest once and receive the resulting score/rank response.

## Business Rules

- `GET /api/v1/quests/active` returns quests with `status = ACTIVE` and adds a per-user `completed` flag.
- `GET /api/v1/quests/{id}` loads by id only; it can return non-active quests if they exist in the table.
- `POST /api/v1/quests/{id}/complete` only loads quests where `status = ACTIVE`.
- A quest with a non-null `deadline` cannot be completed after `deadline.isBefore(Instant.now())`.
- A user can complete a quest only once; `quest_completions` enforces uniqueness on `(user_id, quest_id)`.
- Completing a quest writes a `QuestCompletion` row, adds `quest.xpReward` to `users.totalScore`, then calls `RankUseCaseService.updateUserRank(...)`.
- Despite the field name `xpReward`, quest completion does **not** write to `users.xp`; it updates `users.totalScore`.
- There is no pagination on the active-quest list.

## API

| Method | Path | Auth | Request | Response | Status |
|---|---|---|---|---|---|
| `GET` | `/api/v1/quests/active` | JWT required | — | `QuestListResponse` (`quests`, `completedCount`, `totalCount`) | `200` |
| `GET` | `/api/v1/quests/{id}` | JWT required | Path `id` | `QuestResponse` (`id`, `title`, `description`, `xpReward`, `category`, `status`, `deadline`, `completed`) | `200 / 404` |
| `POST` | `/api/v1/quests/{id}/complete` | JWT required | Path `id` | `QuestCompletionResponse` (`questId`, `questTitle`, `xpAwarded`, `newTotalScore`, `rankTier`) | `200 / 400 / 404 / 409` |

## Data Model

| Table | Entity / owner | Key columns | Constraints / source |
|---|---|---|---|
| `quests` | `quest/adapter/out/persistence/QuestEntity.java` | `id`, `title`, `description`, `xp_reward`, `category`, `status`, `deadline`, `created_at` | Index on `status`; created by `src/main/resources/db/migration/V6__create_quests_tables.sql` |
| `quest_completions` | `quest/adapter/out/persistence/QuestCompletionEntity.java` | `id`, `user_id`, `quest_id`, `completed_at` | Unique `(user_id, quest_id)` plus indexes on `user_id` and `quest_id`; created by `src/main/resources/db/migration/V6__create_quests_tables.sql` |
| `users` | `user/adapter/out/persistence/UserEntity.java` | `id`, `total_score`, `rank_tier` | Quest rewards target `total_score`; rank recalculation uses `rank_tier` |

## Domain Logic

`quest/application/usecase/QuestUseCaseService.java` drives all three endpoints. The read path loads all active quests, loads the user's completed quest ids once, and projects each quest into `QuestResponse`. The write path validates active status, deadline, and uniqueness; persists `quest_completions`; increments the mutable `User.totalScore`; saves the user; and then delegates rank recalculation to `ranking/application/usecase/RankUseCaseService.java`.

## Events

None. Quest completion mutates `users.totalScore` and calls `RankUseCaseService` directly; it does not publish XP, feed, or achievement events.

## Dependencies

This feature depends on `user/` for the score-bearing aggregate and `ranking/` for rank-tier recalculation. `leaderboard/` and friend/profile surfaces then see the new `totalScore` via their normal reads.

## Key Files

| File | Role |
|---|---|
| `quest/adapter/in/rest/QuestController.java` | `/api/v1/quests/**` REST API |
| `quest/application/usecase/QuestUseCaseService.java` | Active-list, lookup, and completion logic |
| `quest/application/port/{QuestRepository,QuestCompletionRepository}.java` | Hexagonal outbound ports |
| `quest/adapter/out/persistence/{QuestEntity,QuestCompletionEntity}.java` | Table mappings |
| `quest/adapter/out/persistence/{QuestJpaRepository,QuestCompletionJpaRepository}.java` | JPA queries |
| `quest/domain/model/{Quest,QuestCompletion,QuestStatus}.java` | Core domain models |
| `quest/adapter/in/rest/dto/{QuestResponse,QuestListResponse,QuestCompletionResponse}.java` | REST payloads |

## Known Limitations

- There are no Flyway seed inserts for `quests` and no admin/create endpoint in `quest/adapter/in/rest/`; on a fresh database, `/api/v1/quests/active` will be empty until quests are inserted externally.
- `getActiveQuests()` filters only on `status = ACTIVE`, so a past-deadline quest still appears in `/api/v1/quests/active` until some external process changes its status. Deadline enforcement happens only when the user tries to complete the quest.
- `QuestCompletionResponse.rankTier` can be stale after a tier change because `QuestUseCaseService.completeQuest()` builds the response from the pre-`updateUserRank()` `User` object it fetched before calling `RankUseCaseService`.

## Future Work

- Quests are currently system-created "for now" (by design, per original commit intent) — an admin/seed-management path is the obvious next step if quest content needs to be managed inside the app.
