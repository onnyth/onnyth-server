# S2-01 — Life Stats CRUD & Bulk Input

> **Status**: `DONE`
> **Priority**: `HIGH`
> **Sprint**: Sprint 2

## User Story

> As a user, I want to input my life stats across 5 categories, update them with history tracking, and have my score auto-recalculate.

## Acceptance Criteria

- [x] 5 stat categories with weights: Career (1.2), Wealth (1.0), Fitness (1.1), Education (1.3), Social Influence (0.9)
- [x] Save single stat (upsert — create or update)
- [x] Bulk save multiple stats at once (onboarding flow)
- [x] Update existing stat with previous-value tracking
- [x] Append-only history log for every stat update (with optional reason)
- [x] Values validated: 1–100 range at both DTO and service level
- [x] `StatChangedEvent` published after every stat change

## Delivered Components

| Component | File |
|---|---|
| Entities | `LifeStat.java`, `LifeStatHistory.java`, `StatCategory.java` |
| Migrations | `V2__create_life_stats_table.sql`, `V3__add_previous_value_and_history.sql` |
| Service | `LifeStatService.java` |
| Controller | `LifeStatController.java` (4 endpoints) |
| Repositories | `LifeStatRepository.java`, `LifeStatHistoryRepository.java` |
| DTOs | `StatInputRequest`, `BulkStatInputRequest`, `StatUpdateRequest`, `LifeStatResponse`, `StatUpdateResponse` |
| Event | `StatChangedEvent.java` |
| Exceptions | `InvalidStatValueException`, `StatNotFoundException` |

## Skills Updated

- `life-stats/SKILL.md` — Created
- `data-layer/SKILL.md` — Added LifeStat, LifeStatHistory entities and V2/V3 migrations
- `api-reference/SKILL.md` — Added stats endpoints
- `error-handling/SKILL.md` — Added stat-related exceptions
