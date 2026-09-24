# Database Schema

Complete table inventory as of this writing, cross-checked against `@Table` annotations in
`src/main/java` and both migration directories. See `docs/data/overview.md` for why there are two
migration histories and why the "Migration" column below matters.

## Table inventory

| Table | Entity class | Owning feature | Created by |
|---|---|---|---|
| `users` | `UserEntity` | `user` | Supabase baseline (`20260412183914_remote_schema.sql`); altered by Flyway `V1,V4,V5,V16,V22` |
| `life_stats` | *(none — orphaned)* | *(none — orphaned)* | Flyway `V2` |
| `life_stat_history` | *(none — orphaned)* | *(none — orphaned)* | Flyway `V3` |
| `quests` | `QuestEntity` | `quest` | Flyway `V6` |
| `quest_completions` | `QuestCompletionEntity` | `quest` | Flyway `V6` |
| `friend_requests` | `FriendRequestEntity` | `friendship` | Flyway `V7` |
| `friendships` | `FriendshipEntity` | `friendship` | Flyway `V8` |
| `follows` | `FollowEntity` | `friendship` (not user-facing) | Supabase baseline only — **no Flyway migration** |
| `leaderboard_snapshots` | `LeaderboardSnapshotEntity` | `leaderboard` | Flyway `V9` |
| `achievements` | `AchievementEntity` | `achievement` | Flyway `V10`, seeded `V13` |
| `user_achievements` | `UserAchievementEntity` | `achievement` | Flyway `V11` |
| `user_displayed_achievements` | *(`@ElementCollection` on `User`)* | `user` | Flyway `V12` |
| `activity_types` | `ActivityTypeEntity` | `activity` | Flyway `V14`, seeded `V20` |
| `activity_log` | `ActivityLogEntity` | `activity` | Flyway `V15` |
| `user_streaks` | `UserStreakEntity` | `streak` | Flyway `V17` |
| `feed_events` | `FeedEventEntity` | `feed` | Flyway `V18` |
| `cosmetic_items` | `CosmeticItemEntity` | `store` | Flyway `V19`, seeded `V24` |
| `user_cosmetics` | `UserCosmeticEntity` | `store` | Flyway `V19` |
| `user_occupation` | `UserOccupationEntity` | `lifestats` | Supabase baseline; altered by Flyway `V21` — **create statement not in Flyway** |
| `user_wealth` | `UserWealthEntity` | `lifestats` | Supabase baseline; altered by Flyway `V21` — **create statement not in Flyway** |
| `user_physique` | `UserPhysiqueEntity` | `lifestats` | Supabase baseline only — **no Flyway migration** |
| `user_wisdom` | `UserWisdomEntity` | `lifestats` | Supabase baseline; altered by Flyway `V21` — **create statement not in Flyway** |
| `user_charisma` | `UserCharismaEntity` | `lifestats` | Supabase baseline only — **no Flyway migration** |
| `user_social_accounts` | `UserSocialAccountEntity` | `lifestats` | Supabase baseline only — **no Flyway migration** |
| `user_xfactors` | `UserXfactorEntity` | `lifestats` | Supabase baseline only — **no Flyway migration** |
| `user_education` | `UserEducationEntity` | `lifestats` | Supabase baseline only — **no Flyway migration** |
| `profile_likes` | `ProfileLikeEntity` | `lifestats` | Supabase baseline only — **no Flyway migration** |
| `score_history` | `ScoreHistoryEntity` | `scoring` | Supabase baseline only — **no Flyway migration** |
| `sport_medals` | `SportMedalEntity` | `scoring` | Supabase baseline only — **no Flyway migration** |
| `registration_drafts` | `RegistrationDraftEntity` | `registration` | Supabase migration `20260419_registration_drafts.sql` only — **no Flyway migration** |
| `profile_votes` | `ProfileVoteEntity` | `profile` | Flyway `V23` |
| `bookmark` / `bookmark_tags` | `BookmarkEntity` | `bookmark` | **No migration anywhere** (neither Flyway nor Supabase) |
| `posts` | `Post` (unwired placeholder) | `models` | Supabase baseline only |
| `comments` | `Comment` (unwired placeholder) | `models` | Supabase baseline only |
| `likes` | `Like` (unwired placeholder) | `models` | Supabase baseline only |

**17 of ~33 tables** are created by a Flyway migration; the rest exist only because of the Supabase
baseline dump or a Supabase-only migration. See `docs/development/known-issues.md` for the risk this
poses and `docs/data/overview.md` for the mechanism.

## Flyway migration ledger (`src/main/resources/db/migration/`)

| Migration | Description |
|---|---|
| `V1__add_profile_completion_fields.sql` | Adds `profile_complete`, `updated_at` to `users`; constrains `username` to unique VARCHAR(20) |
| `V2__create_life_stats_table.sql` | Creates `life_stats` (orphaned — see below) |
| `V3__add_previous_value_and_history.sql` | Adds `previous_value` to `life_stats`; creates `life_stat_history` (orphaned) |
| `V4__add_total_score_to_users.sql` | Adds `total_score BIGINT DEFAULT 0` to `users` |
| `V5__add_rank_tier_to_users.sql` | Adds `rank_tier VARCHAR(20) DEFAULT 'BRONZE'` to `users` |
| `V6__create_quests_tables.sql` | Creates `quests`, `quest_completions` |
| `V7__create_friend_request_table.sql` | Creates `friend_requests` |
| `V8__create_friendship_table.sql` | Creates `friendships` |
| `V9__create_leaderboard_snapshots_table.sql` | Creates `leaderboard_snapshots` |
| `V10__create_achievements_table.sql` | Creates `achievements` |
| `V11__create_user_achievements_table.sql` | Creates `user_achievements` |
| `V12__add_displayed_achievements_to_users.sql` | Creates `user_displayed_achievements` |
| `V13__seed_initial_achievements.sql` | Seeds 12 default achievements |
| `V14__create_activity_types_table.sql` | Creates `activity_types` |
| `V15__create_activity_log_table.sql` | Creates `activity_log` |
| `V16__add_xp_level_to_users.sql` | Adds `xp` (BIGINT), `level` (INT) to `users` |
| `V17__create_user_streaks_table.sql` | Creates `user_streaks` |
| `V18__create_feed_events_table.sql` | Creates `feed_events` |
| `V19__create_cosmetic_tables.sql` | Creates `cosmetic_items`, `user_cosmetics` |
| `V20__seed_activity_types.sql` | Seeds 25 activity types |
| `V21__add_structured_onboarding_fields.sql` | **Alters** (does not create) `user_occupation`, `user_wealth`, `user_wisdom` |
| `V22__add_profile_cosmetic_and_ranking_fields.sql` | Adds `world_rank`, `country_rank`, `country`, vote score, active-cosmetic FKs to `users` |
| `V23__create_profile_votes_table.sql` | Creates `profile_votes` |
| `V24__seed_frame_and_background_cosmetics.sql` | Adds `BACKGROUND`/`FRAME` cosmetic categories, seeds default free items |

**Next Flyway migration should be named** `V25__<description>.sql`.

## Supabase migration ledger (`supabase/migrations/`)

| Migration | Description |
|---|---|
| `20260412183914_remote_schema.sql` | Full baseline snapshot (`supabase db pull`) — creates 29 tables, see above |
| `20260413_add_income_currency.sql` | Adds income currency column(s) (mirrors part of Flyway `V21`-era work) |
| `20260419_registration_drafts.sql` | Creates `registration_drafts` — **no Flyway equivalent** |
| `20260503115343_add_structured.sql` | Mirrors Flyway `V21__add_structured_onboarding_fields.sql` |
| `20260505011410_add_profile_cosmetic.sql` | Mirrors Flyway `V22` |
| `20260505011425_create_profile_votes.sql` | Mirrors Flyway `V23` |
| `20260505011432_seed_frame.sql` | Mirrors Flyway `V24` |
| `20260507_seed_activity_types.sql` | Mirrors Flyway `V20` (out of chronological order — added later) |

## Orphaned tables: `life_stats` / `life_stat_history`

These tables (created by `V2`/`V3`) are **no longer written by any current application code** — there
is no `LifeStat`/`LifeStatHistory` domain class or entity anymore. They were superseded by the
structured per-domain tables (`user_occupation`, `user_wealth`, etc.) during the registration/lifestats
rework. No migration drops them; they remain in the schema unused. See
`docs/development/known-issues.md`.

## Enums used across the schema

| Enum | Values | Owning feature |
|---|---|---|
| `RankTier` | `BRONZE, SILVER, GOLD, PLATINUM, ELITE` (verify exact list/thresholds in `docs/features/ranking.md`) | `ranking` |
| `StatDomain` | `OCCUPATION, WEALTH, PHYSIQUE, WISDOM, CHARISMA` | `shared` |
| `QuestStatus` | `ACTIVE, EXPIRED, ARCHIVED` | `quest` |
| `FriendRequestStatus` | `PENDING, ACCEPTED, REJECTED` | `friendship` |
| `AchievementCategory` | `STATS, SOCIAL, STREAK, MILESTONE, SPECIAL` | `achievement` |
| `ActivityFrequency` | `DAILY, WEEKLY` | `activity` |
| `FeedEventType` | `ACTIVITY, LEVEL_UP, ACHIEVEMENT, STREAK` (verify in `docs/features/feed.md`) | `feed` |
| `CosmeticCategory` | `PROFILE_THEME, TITLE, BADGE_FRAME, AVATAR_SKIN, GLOW_EFFECT, BANNER`, plus `BACKGROUND`/`FRAME` added in `V24` | `store` |
| `CosmeticRarity` | `COMMON, RARE, EPIC, LEGENDARY` | `store` |

## Configuration

```properties
spring.jpa.hibernate.ddl-auto=validate           # Schema managed externally (Flyway + Supabase), never by Hibernate
spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect
```

## Adding a new entity

1. Create the JPA entity in `{feature}/adapter/out/persistence/`.
2. Create a Flyway migration `V{next}__description.sql` **and** a mirrored `supabase/migrations/*.sql`
   entry — see `docs/data/overview.md` for why both are currently expected.
3. Create the Spring Data JPA repository + port + adapter (see
   `docs/architecture/low-level-design.md`).
4. Use `UUID` primary keys (`@GeneratedValue(strategy = GenerationType.AUTO)` or app-assigned) —
   consistent with every existing entity.
