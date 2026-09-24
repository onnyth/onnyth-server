# Scoring

## Status

Partially Implemented
The scoring formulas, repositories, and persistence objects exist, but no controller or event-listener
wiring currently invokes `scoring/ScoreCalculationUseCaseService` from live registration/stat-change flows.

## Purpose

Transforms a user's structured life-stat rows into five normalized 0-100 domain scores plus one weighted
`users.totalScore`. It also records audit history for score changes and reads medal / social / follow data
so profile progression is not tied to a single table.

## User Capabilities

- See stored domain scores on profile cards once the relevant stat rows have been scored.
- See stored `totalScore` anywhere the API returns a profile card, friend profile, leaderboard row, or rank
  progress response.
- There is no direct score-recalculation endpoint in the current backend.

## Business Rules

- Domain weights come from `shared/domain/model/StatDomain.java`:

  | Domain | Weight |
  |---|---:|
  | `OCCUPATION` | 1.2 |
  | `WEALTH` | 1.0 |
  | `PHYSIQUE` | 1.1 |
  | `WISDOM` | 1.3 |
  | `CHARISMA` | 0.9 |

- Weighted total formula:
  `totalScore = round(occupation*1.2 + wealth*1.0 + physique*1.1 + wisdom*1.3 + charisma*0.9)`.
  With each domain capped at 100, the pure stat-based maximum is **550**.
- Domain formulas (each capped at 100):

  | Domain | Inputs actually used by code | Formula |
  |---|---|---|
  | `OCCUPATION` | `jobTitle`, `yearsExperience`, `skills` | `titleTier + 15 + min(yearsExperience*2, 20) + min(skillCount*3, 15)` |
  | `WEALTH` | `incomeBracket`, `users.onnythCoins`, `monthlySavingPct`, `incomeVerified` | `incomeBracketPoints + min(onnythCoins/100, 35) + floor(monthlySavingPct/100*15) + (incomeVerified ? 10 : 0)` |
  | `PHYSIQUE` | `fitnessLevel`, `bodyFatPct`, `weeklyWorkouts`, `sport_medals` | `fitnessLevelPoints + bodyCompPoints + min(weeklyWorkouts*5, 25) + cappedMedalPoints` |
  | `WISDOM` | highest `user_education.level`, `habitIds`, `user_xfactors` | `educationPoints + min(habitCount*3, 15) + cappedXfactorPoints` |
  | `CHARISMA` | `user_social_accounts.followerCount`, follow count, `profile_likes`, verified social accounts | `min(log10(totalFollowers+1)*10, 35) + min(onnythFollowers*2, 30) + min(profileLikes, 20) + min(verifiedAccounts*5, 15)` |

- Exact scoring tables / helper rules:
  - Occupation title tier: `CEO/CTO/CFO/chief/founder/co-founder=40`, `VP/vice president=35`,
    `director=30`, `head/principal=28`, `lead/staff=25`, `senior/sr=20`,
    `mid/engineer/developer/analyst/designer/manager=15`, `junior/jr/associate=10`, `intern=5`,
    default `10`.
  - `IncomeBracket` points: `UNDER_25K=5`, `25K_50K=10`, `50K_75K=15`, `75K_100K=20`,
    `100K_150K=25`, `150K_250K=30`, `250K_500K=35`, `OVER_500K=40`.
  - `FitnessLevel` points: `BEGINNER=5`, `INTERMEDIATE=15`, `ADVANCED=25`, `ATHLETE=30`, `ELITE=30`.
  - Body-fat helper: `null => 10`, `10-22 => 25`, `6-28 => 20`, `3-35 => 15`, otherwise `10`.
  - Medal points: `GOLD=10`, `SILVER=7`, `BRONZE=5`, `CHAMPIONSHIP=12`, `PARTICIPATION=2`, then cap at 20.
  - `EducationLevel` points: `HIGH_SCHOOL=5`, `BOOTCAMP=10`, `SELF_TAUGHT=10`, `ASSOCIATE=12`,
    `CERTIFICATION=15`, `BACHELORS=20`, `MASTERS=28`, `PHD=35`.
  - X-factor helper: each row starts at `8`; add `min(log10(metricValue)*5, 15)` when `metricValue > 0`;
    double the row subtotal when `isVerified=true`; cap domain total at 50.

## API

No dedicated `scoring/` REST controller exists. Stored scores are exposed indirectly through profile,
friendship, and leaderboard responses.

## Data Model

| Table | Entity class | Key columns / constraints | Migration(s) |
|---|---|---|---|
| `users` | `user/adapter/out/persistence/UserEntity.java` | `total_score BIGINT NOT NULL DEFAULT 0` | `src/main/resources/db/migration/V4__add_total_score_to_users.sql` |
| `score_history` | `scoring/adapter/out/persistence/ScoreHistoryEntity.java` | PK `id`; FK `user_id`; `domain` CHECK over `OCCUPATION/WEALTH/PHYSIQUE/WISDOM/CHARISMA/TOTAL`; indexes on `(user_id)` and `(user_id, domain, changed_at)` | `supabase/migrations/20260412183914_remote_schema.sql` |
| `sport_medals` | `scoring/adapter/out/persistence/SportMedalEntity.java` | PK `id`; FK `user_id`; medal-type CHECK; year CHECK `1950..2100`; index on `user_id` | `supabase/migrations/20260412183914_remote_schema.sql` |
| `user_occupation`, `user_wealth`, `user_physique`, `user_wisdom`, `user_charisma`, `user_education`, `user_social_accounts`, `user_xfactors`, `profile_likes` | `lifestats/adapter/out/persistence/*Entity.java` | Input tables read by the calculators | See `docs/features/life-stats.md` |

## Domain Logic

```text
recalculateAll(userId)
  -> recalculateOccupation(userId)
  -> recalculateWealth(userId)
  -> recalculatePhysique(userId)
  -> recalculateWisdom(userId)
  -> recalculateCharisma(userId)
  -> weighted round() into users.totalScore
  -> append ScoreHistory rows when values changed
```

Each domain recalculator mutates the corresponding row's stored `score` only when the numeric result
changed; `persistDomainScore(...)` is the shared helper that updates the entity and appends a
`ScoreHistory` row with reason `"Domain recalculation"`.

## Events

None currently wired. `shared/domain/event/StatChangedEvent.java` exists as `record StatChangedEvent(UUID
userId)`, but `scoring/` does not declare an `@EventListener` for it and no scoring class publishes a
follow-up event.

## Dependencies

- **`lifestats/`** — primary stat input tables.
- **`user/`** — owns `users.totalScore` and `users.onnythCoins`.
- **`friendship/FollowRepository`** — supplies Onnyth follower counts for charisma scoring.
- **Consumers**: `ranking/`, `leaderboard/`, `profile/`, `friendship/`, and quests all read the stored
  `totalScore` / domain scores.

## Key Files

| File | Role |
|---|---|
| `shared/domain/model/StatDomain.java` | Shared domain-weight contract |
| `scoring/application/usecase/ScoreCalculationUseCaseService.java` | All domain formulas plus weighted total recalculation |
| `scoring/domain/model/{ScoreHistory,SportMedal,MedalType}.java` | Scoring domain objects |
| `scoring/application/port/{ScoreHistoryRepository,SportMedalRepository}.java` | Outbound ports for scoring-owned tables |
| `scoring/adapter/out/persistence/{ScoreHistoryEntity,ScoreHistoryJpaRepository,ScoreHistoryRepositoryAdapter}.java` | Audit persistence |
| `scoring/adapter/out/persistence/{SportMedalEntity,SportMedalJpaRepository,SportMedalRepositoryAdapter}.java` | Medal persistence used by physique scoring |

## Known Limitations

- `ScoreCalculationUseCaseService.recalculateAll(...)` has no call sites in the current source, so scores are
  not automatically refreshed after registration or later stat changes.
- `recalculateAll(...)` tries to write a total-score history row with `domain=null`, but
  `ScoreHistoryEntity.domain` is `nullable = false` and uses `StatDomain`, which has no `TOTAL` constant.
  If this path runs and `totalScore` changes, the history insert is inconsistent with the schema.
- Several onboarding fields are stored but not scored: `UserOccupation.companyName/raw* /isVerified`,
  `UserWealth.netWorthBracket/monthlySpendingBracket/incomeCurrency`, `UserPhysique.heightCm/weightKg`,
  `UserWisdom.languages/educationLevel/institutionName/graduationYear`, and
  `UserCharisma.relationshipStatus/socialCircleSize` are ignored by current formulas.
- The wisdom calculator reads `user_education` for education points, but registration writes education-like
  data only into `user_wisdom`; without a separate `user_education` row, wisdom education points stay `0`.
- `quest/application/usecase/QuestUseCaseService.java` mutates `users.totalScore` directly by quest
  `xpReward`, bypassing these formulas and making `totalScore` a mixed concept.
- Charisma scoring counts `profile_likes`, while the user-facing vote API now writes `profile_votes`.

## Future Work

Sprint 2's scoring/ranking story explicitly called for event-driven `StatChangedEvent` recalculation; current
source still needs that wiring (or another invoker) to make these formulas live.
