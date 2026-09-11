# S2-02 — Scoring, Ranking & Profile Card

> **Status**: `DONE`
> **Priority**: `HIGH`
> **Sprint**: Sprint 2

## User Story

> As a user, I want my life stats to generate a weighted score and earn RPG rank tiers so that I feel a sense of progression.

## Acceptance Criteria

- [x] Weighted score formula: `Σ(value × weight)`, persisted as `totalScore` on User
- [x] Score auto-recalculates via `StatChangedEvent` → `@EventListener`
- [x] 5-tier rank system: BRONZE (0) → SILVER (100) → GOLD (250) → PLATINUM (500) → ELITE (1000)
- [x] Rank tier persisted on User, updated only when it actually changes
- [x] Rank progress endpoint showing current tier, next tier, points needed, progress %
- [x] Profile card includes total score and rank tier
- [x] Public profile card endpoint accessible without authentication

## Delivered Components

| Component | File |
|---|---|
| Enum | `RankTier.java` (5 tiers with `fromScore()`, `nextTier()`) |
| Services | `ScoreCalculationService.java`, `RankService.java` |
| Migration | `V4__add_total_score_to_users.sql`, `V5__add_rank_tier_to_users.sql` |
| DTOs | `RankProgressResponse.java`, `ProfileCardResponse.java` (updated) |
| Endpoints | `GET /api/v1/profile/rank`, `GET /api/v1/profile/card`, `GET /api/v1/users/{id}/card` |

## Skills Updated

- `scoring-and-ranking/SKILL.md` — Created
- `data-layer/SKILL.md` — Added totalScore, rankTier to User entity, V4/V5 migrations
- `api-reference/SKILL.md` — Added rank progress and profile card endpoints
