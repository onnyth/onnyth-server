---
name: scoring-and-ranking
description: Weighted score calculation, event-driven recalculation, and RPG-style 5-tier rank system
---

# Scoring & Ranking

Users earn a weighted "life score" from their 5 stat categories and progress through RPG-style rank tiers. Score recalculation is fully event-driven.

## Key Files

| File | Role |
|---|---|
| `service/ScoreCalculationService.java` | Weighted score formula + event listener |
| `service/RankService.java` | Rank tier calculation, persistence, and progress |
| `models/RankTier.java` | 5-tier enum with score thresholds |
| `events/StatChangedEvent.java` | Event record triggering recalculation |
| `dto/RankProgressResponse.java` | Rank progress DTO with progress percentage |

## Score Calculation Formula

```
totalScore = Σ(statValue × categoryWeight)    rounded to nearest long
```

Category weights:
| Category | Weight | Max Contribution |
|---|---|---|
| EDUCATION | 1.3 | 130 |
| CAREER | 1.2 | 120 |
| FITNESS | 1.1 | 110 |
| WEALTH | 1.0 | 100 |
| SOCIAL_INFLUENCE | 0.9 | 90 |

**Max possible score**: 550 (all categories at 100)

## Event-Driven Recalculation

```java
// ScoreCalculationService.java
@EventListener
@Transactional
public void onStatChanged(StatChangedEvent event) {
    recalculateUserScore(event.userId());
}
```

The `recalculateUserScore` method:
1. Fetches all stats for the user
2. Calculates weighted score
3. Persists `totalScore` on the `User` entity
4. Calls `RankService.updateUserRank()` to update rank tier

## Rank Tiers

```java
public enum RankTier {
    BRONZE(0,    "Bronze",   "🥉"),
    SILVER(100,  "Silver",   "🥈"),
    GOLD(250,    "Gold",     "🥇"),
    PLATINUM(500,"Platinum", "💎"),
    ELITE(1000,  "Elite",    "👑");
}
```

### Key Methods

| Method | Description |
|---|---|
| `RankTier.fromScore(long)` | Returns the highest tier where `totalScore >= minScore` |
| `RankTier.nextTier()` | Returns the next tier above, or `null` if at ELITE |

### RankService Operations

| Method | Description |
|---|---|
| `calculateRankTier(long score)` | Pure function — delegates to `RankTier.fromScore()` |
| `updateUserRank(UUID userId)` | Recalculates + persists rank only if tier actually changed |
| `getRankProgress(UUID userId)` | Returns `RankProgressResponse` with progress % to next tier |

## Rank Progress Response

The `RankProgressResponse` includes:
- `currentTier` / `currentBadge` — display name and emoji
- `currentScore` — user's total score
- `nextTier` / `nextBadge` — next tier info (null if at ELITE)
- `pointsToNextTier` — points remaining
- `progressPercent` — % progress within current tier (0.0—100.0, rounded to 1 decimal)

Progress formula:
```
progress = (totalScore - currentTier.minScore) / (nextTier.minScore - currentTier.minScore) × 100
```

## Endpoint

`GET /api/v1/profile/rank` — returns `RankProgressResponse`

## Adding New Rank Features

- **New tiers**: Add enum constants to `RankTier` (maintain ascending score order)
- **Score formula changes**: Modify `ScoreCalculationService.calculateScore()`
- **Rank rewards**: Hook into `RankService.updateUserRank()` where the old→new tier change is detected
