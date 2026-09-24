# Ranking

## Status

Partially Implemented
Tier calculation, progress responses, and scheduled world/country rank caching exist, but `rank_tier` is
not automatically refreshed when `totalScore` changes through the current registration/scoring paths.

## Purpose

Converts a stored total life score into RPG-style progression markers. The feature persists the user's tier
on the `users` row, computes human-readable progress to the next tier, and periodically caches global and
country leaderboard positions for cheap profile-card reads.

## User Capabilities

- Fetch their current tier, badge, points to next tier, and progress percentage.
- See rank tier, badge, world rank, and country rank in profile-card responses.
- Benefit from periodically refreshed cached global and country ranks without running live aggregate queries.

## Business Rules

- Tier thresholds (`ranking/domain/model/RankTier.java`):

  | Tier | Minimum `totalScore` | Badge |
  |---|---:|---|
  | `BRONZE` | 0 | 🥉 |
  | `SILVER` | 100 | 🥈 |
  | `GOLD` | 250 | 🥇 |
  | `PLATINUM` | 500 | 💎 |
  | `ELITE` | 1000 | 👑 |

- `RankTier.fromScore(totalScore)` returns the highest tier whose `minScore <= totalScore`.
- `RankProgressResponse.fromScoreAndTier(...)` computes progress within the current tier band:
  - `pointsToNextTier = next.minScore - totalScore`.
  - `progressPercent = (totalScore - tier.minScore) / (next.minScore - tier.minScore) * 100`, rounded to
    one decimal place.
  - At `ELITE`, `nextTier` / `nextBadge` become `null`, `pointsToNextTier=0`, `progressPercent=100.0`.
- Cached leaderboard positions are 1-based and recomputed by scheduled jobs:
  - world ranks: every `onnyth.ranking.interval-ms` (default `900000` ms / 15 min), initial delay 5 s.
  - country ranks: same interval, initial delay 65 s.
- Country ranks only include users whose `users.country` is non-null.

## API

| Method | Path | Auth | Request | Response | Status |
|---|---|---|---|---|---|
| `GET` | `/api/v1/profile/rank` | JWT | — | `RankProgressResponse { currentTier, currentBadge, currentScore, nextTier, nextBadge, pointsToNextTier, progressPercent }` | 200 |

Related read surfaces: `rankTier`, `rankBadgeUrl`, `worldRank`, and `countryRank` are also embedded in
`ProfileCardResponse` from `/api/v1/profile/card`, `/api/v1/profile/{userId}/card`, and public
`/api/v1/users/{userId}/card`.

## Data Model

| Table | Entity class | Key columns / constraints | Migration(s) |
|---|---|---|---|
| `users` | `user/adapter/out/persistence/UserEntity.java` | `rank_tier VARCHAR(20) NOT NULL DEFAULT 'BRONZE'`; `world_rank`, `country_rank`, `country` nullable cached fields; index on `total_score DESC` and `country` | `src/main/resources/db/migration/V5__add_rank_tier_to_users.sql`, `src/main/resources/db/migration/V22__add_profile_cosmetic_and_ranking_fields.sql` |

## Domain Logic

```text
RankUseCaseService.updateUserRank(userId)
  -> load User.totalScore
  -> RankTier.fromScore(totalScore)
  -> persist new rank_tier only if it changed

RankingUseCaseService.computeWorldRanks()
  -> findAllOrderedByScoreDesc()
  -> assign worldRank = index + 1
  -> saveAll(users)

RankingUseCaseService.computeCountryRanks()
  -> for each distinct country
  -> findByCountryOrderByScoreDesc(country)
  -> assign countryRank = index + 1
  -> saveAll(countryUsers)
```

`GET /api/v1/profile/rank` does not recompute the tier; it trusts the persisted `users.rankTier` plus the
current stored `users.totalScore`.

## Events

None.

## Dependencies

- **`user/`** — owns the stored `rankTier`, `worldRank`, `countryRank`, and `country` columns.
- **`scoring/`** — is the intended producer of `users.totalScore`.
- **Consumers**: `profile/`, `friendship/`, and leaderboard-style features read the cached tier/rank data.

## Key Files

| File | Role |
|---|---|
| `ranking/domain/model/RankTier.java` | Tier thresholds, display names, badges, `fromScore()` / `nextTier()` |
| `ranking/application/usecase/RankUseCaseService.java` | Persists `rank_tier` and builds `RankProgressResponse` |
| `ranking/application/usecase/RankingUseCaseService.java` | Scheduled world/country rank caching jobs |
| `ranking/adapter/in/rest/dto/RankProgressResponse.java` | Public rank progress DTO |
| `profile/adapter/in/rest/ProfileController.java` | `/api/v1/profile/rank` endpoint |
| `profile/adapter/in/rest/dto/ProfileCardResponse.java` | Rank and cached position fields exposed to clients |

## Known Limitations

- `RankUseCaseService.updateUserRank(...)` is only called from `quest/application/usecase/QuestUseCaseService.java`
  in the current source. Score changes from registration or scoring recalculation do not automatically update
  the persisted tier.
- Because the pure weighted score formula tops out at 550, `ELITE (1000)` is unreachable from
  `ScoreCalculationUseCaseService` alone. `PLATINUM` is only reachable near the absolute top of the
  stat-based range.
- World/country ranking jobs sort only by `totalScore DESC`; there is no deterministic secondary sort for
  ties, so equal-score ordering may shift between runs.
- `worldRank` / `countryRank` remain `null` until the scheduled jobs have run at least once.

## Future Work

The backlog still carries leaderboard-oriented follow-on work (`5.1 Global leaderboard by total score` as
`💡 IDEA`), which would build on these cached ranking fields and scheduled recomputation jobs.
