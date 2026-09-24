# Life Stats

## Status

Partially Implemented
Structured domain entities, ports, and persistence adapters exist, but `lifestats/` has no use-case
service or REST controller for direct CRUD/query management.

## Purpose

Stores a user's domain-specific real-world profile in normalized tables instead of the old generic
`life_stats` model. The data is split across occupation, wealth, physique, wisdom, charisma, education,
social-account, x-factor, and profile-like records so other features can score, rank, compare, and display
users more precisely.

## User Capabilities

- Populate base occupation, wealth, physique, wisdom, and charisma records indirectly through
  `/api/v1/registration/**`.
- Read five domain score snapshots inside profile-card responses from `/api/v1/profile/card`,
  `/api/v1/profile/{userId}/card`, and public `/api/v1/users/{userId}/card`.
- No current API exists in this backend to directly create/update education entries, social accounts,
  x-factors, sport medals, or profile-like rows.

## Business Rules

- Shared scoring domains come from `shared/domain/model/StatDomain.java`:

  | Domain | Display name | Weight |
  |---|---|---:|
  | `OCCUPATION` | Occupation | 1.2 |
  | `WEALTH` | Wealth | 1.0 |
  | `PHYSIQUE` | Physique | 1.1 |
  | `WISDOM` | Wisdom | 1.3 |
  | `CHARISMA` | Charisma | 0.9 |

- Cardinality by aggregate:
  - `user_occupation`: many rows per user historically, but the Supabase schema enforces one current row
    with partial unique index `uq_user_occupation_current` where `is_current=true`.
  - `user_wealth`, `user_physique`, `user_wisdom`, `user_charisma`: one row per user (`user_id` unique).
  - `user_education`, `user_social_accounts`, `user_xfactors`: one-to-many child tables.
  - `profile_likes`: composite key `(liker_id, liked_id)`; self-like is forbidden.
- Enum-backed value sets enforced by the schema / entities:
  - `EmploymentType`: `FULL_TIME`, `PART_TIME`, `FREELANCE`, `SELF_EMPLOYED`, `UNEMPLOYED`, `STUDENT`.
  - `IncomeBracket`: `UNDER_25K`, `25K_50K`, `50K_75K`, `75K_100K`, `100K_150K`, `150K_250K`, `250K_500K`,
    `OVER_500K`.
  - `FitnessLevel`: `BEGINNER`, `INTERMEDIATE`, `ADVANCED`, `ATHLETE`, `ELITE`.
  - `EducationLevel`: `HIGH_SCHOOL`, `BOOTCAMP`, `SELF_TAUGHT`, `ASSOCIATE`, `CERTIFICATION`,
    `BACHELORS`, `MASTERS`, `PHD`.
  - `SocialPlatform`: `INSTAGRAM`, `LINKEDIN`, `GITHUB`, `YOUTUBE`.
  - `XfactorType`: `YOUTUBE_CHANNEL`, `COMPANY_OWNER`, `NGO_FOUNDER`, `PUBLICATION`, `PATENT`,
    `OPEN_SOURCE`, `PUBLIC_SPEAKING`, `AWARD`, `OTHER`.
- `user_wisdom.hobbies` is intentionally mapped to `habitIds` in Java for backward DB compatibility, while
  `languages` is stored in a separate JSONB column.
- `user_charisma.onnyth_profile_likes` is a denormalized counter; `ProfileUseCaseService` shows `users.voteScore`
  separately, so charisma likes and profile votes are different concepts in the current codebase.

## API

No dedicated REST controller exists under `lifestats/`. Data is written indirectly by `registration/` and
read indirectly through profile-card endpoints in `profile/` and `user/`.

## Data Model

| Table | Entity class | Key columns / constraints | Migration(s) |
|---|---|---|---|
| `user_occupation` | `lifestats/adapter/out/persistence/UserOccupationEntity.java` | PK `id`; FK `user_id`; partial unique index `uq_user_occupation_current` (`user_id` where `is_current=true`); `skills` JSONB; `is_verified`, `raw_job_title`, `raw_company_name` | Base table and partial index in `supabase/migrations/20260412183914_remote_schema.sql`; structured additions in `src/main/resources/db/migration/V21__add_structured_onboarding_fields.sql` |
| `user_wealth` | `lifestats/adapter/out/persistence/UserWealthEntity.java` | PK `id`; unique `user_id`; `income_bracket` CHECK; `monthly_saving_pct` 0-100; `income_currency` default `USD`; `monthly_spending_bracket` | Base table in `supabase/migrations/20260412183914_remote_schema.sql`; `income_currency` in `supabase/migrations/20260413_add_income_currency.sql`; `monthly_spending_bracket` in `src/main/resources/db/migration/V21__add_structured_onboarding_fields.sql` |
| `user_physique` | `lifestats/adapter/out/persistence/UserPhysiqueEntity.java` | PK `id`; unique `user_id`; checks on `height_cm`, `weight_kg`, `body_fat_pct`, `weekly_workouts`, `fitness_level` | `supabase/migrations/20260412183914_remote_schema.sql` |
| `user_wisdom` | `lifestats/adapter/out/persistence/UserWisdomEntity.java` | PK `id`; unique `user_id`; `hobbies` JSONB (`habitIds`), `languages` JSONB; structured education fields added later | Base table in `supabase/migrations/20260412183914_remote_schema.sql`; additions in `src/main/resources/db/migration/V21__add_structured_onboarding_fields.sql` |
| `user_charisma` | `lifestats/adapter/out/persistence/UserCharismaEntity.java` | PK `id`; unique `user_id`; `onnyth_profile_likes >= 0`; `relationship_status`; `social_circle_size` | Base table in `supabase/migrations/20260412183914_remote_schema.sql`; extra columns in `supabase/migrations/20260419_registration_drafts.sql` |
| `user_social_accounts` | `lifestats/adapter/out/persistence/UserSocialAccountEntity.java` | PK `id`; unique `(user_id, platform)`; `follower_count >= 0`; platform CHECK | `supabase/migrations/20260412183914_remote_schema.sql` |
| `user_education` | `lifestats/adapter/out/persistence/UserEducationEntity.java` | PK `id`; FK `user_id`; partial unique index `uq_user_education_highest` (`user_id` where `is_highest=true`); graduation-year CHECK | `supabase/migrations/20260412183914_remote_schema.sql` |
| `user_xfactors` | `lifestats/adapter/out/persistence/UserXfactorEntity.java` | PK `id`; FK `user_id`; type CHECK | `supabase/migrations/20260412183914_remote_schema.sql` |
| `profile_likes` | `lifestats/adapter/out/persistence/ProfileLikeEntity.java` | Composite PK `(liker_id, liked_id)`; both FKs cascade-delete to `users`; self-like CHECK; index on `liked_id` | `supabase/migrations/20260412183914_remote_schema.sql` |

## Domain Logic

`registration/application/usecase/RegistrationCommitUseCaseService.java` is the only code in this backend
that creates the main one-to-one stat rows today. It writes:

- `UserOccupation` from the `OCCUPATION` step.
- `UserWealth` from the `WEALTH` step.
- `UserPhysique` from the `PHYSIQUE` step.
- `UserWisdom` from the `WISDOM` step.
- `UserCharisma` from the `CHARISMA` step.

`profile/application/usecase/ProfileUseCaseService.java` is the main read path: it loads those one-to-one
rows, converts each stored `score` into a `DomainScoreDto`, and falls back to `score=0`, `label="Not set"`,
`rankBadge="—"` when a row is missing. `UserEducation`, `UserSocialAccount`, `UserXfactor`, and
`ProfileLike` are currently persistence-only building blocks consumed by `scoring/`, not by dedicated
lifestat use cases.

## Events

`shared/domain/event/StatChangedEvent.java` defines `record StatChangedEvent(UUID userId)`, but no class in
`lifestats/` publishes it in the current source.

## Dependencies

- **`registration/`** writes the initial structured rows.
- **`scoring/`** reads the lifestat repositories to calculate stored domain scores and `users.totalScore`.
- **`profile/`** reads stored domain scores to build `ProfileCardResponse`.
- **`achievement/`, `leaderboard/`, `friendship/`** also read stat repositories for comparisons and progress.

## Key Files

| File | Role |
|---|---|
| `shared/domain/model/StatDomain.java` | Shared 5-domain contract and weight table |
| `shared/domain/event/StatChangedEvent.java` | Shared-but-currently-unused stat-change event contract |
| `lifestats/domain/model/{UserOccupation,UserWealth,UserPhysique,UserWisdom,UserCharisma,UserEducation,UserSocialAccount,UserXfactor,ProfileLike}.java` | Framework-free domain models |
| `lifestats/adapter/out/persistence/*Entity.java` | JPA table mappings for all stat aggregates |
| `lifestats/application/port/*.java` | Outbound ports used by scoring/profile/registration |
| `profile/application/usecase/ProfileUseCaseService.java` | Main read-side assembler for domain score snapshots |
| `registration/application/usecase/RegistrationCommitUseCaseService.java` | Main write-side creator of one-to-one stat rows |

## Known Limitations

- The old generic schema is still present in Flyway (`V2__create_life_stats_table.sql`,
  `V3__add_previous_value_and_history.sql`), but no current Java code reads or writes `life_stats` or
  `life_stat_history`.
- `.agents/skills/life-stats/SKILL.md` is stale: it documents removed `LifeStat` / `LifeStatHistory`
  classes and `/api/v1/stats` endpoints. The live code uses the structured entities above instead.
- `lifestats/` still contains `InvalidStatValueException` and `StatNotFoundException`, but there is no
  controller or use-case service that throws them.
- The package contains `ProfileLike` / `profile_likes`, while the newer user-facing vote API lives in
  `profile/` and writes `profile_votes`. Charisma scoring still counts `profile_likes`, not `profile_votes`.
- Registration only populates the five one-to-one rows; there is no backend path here for entering
  `user_education`, `user_social_accounts`, `user_xfactors`, or `profile_likes`.

## Future Work

Sprint 2's earlier CRUD/history story expected direct stat CRUD, history tracking, and automatic stat-change
events, but no active backlog item currently re-scopes that work onto the new structured model.
