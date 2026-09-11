---
name: data-layer
description: JPA entities, repositories, Flyway migrations, and database schema for the Onnyth Server
---

# Data Layer

PostgreSQL database managed through JPA entities and Flyway migrations. Database is hosted on Supabase.

## JPA Entities

### Core Entities (fully wired)

#### User (`users` table)
| Column | Type | Constraints |
|---|---|---|
| `id` | UUID | PK, not null, not updatable |
| `username` | VARCHAR(20) | unique, 3-20 chars, `[a-zA-Z0-9_]` |
| `email` | TEXT | not null, unique |
| `full_name` | VARCHAR(100) | max 100 chars |
| `profile_pic` | TEXT | URL to Supabase Storage |
| `email_verified` | BOOLEAN | not null, not updatable |
| `profile_complete` | BOOLEAN | not null, default `false` |
| `total_score` | BIGINT | not null, default `0` |
| `xp` | BIGINT | not null, default `0` |
| `level` | INTEGER | not null, default `1` |
| `rank_tier` | VARCHAR(20) | not null, default `'BRONZE'`, stored as STRING |
| `created_at` | TIMESTAMPTZ | not null, not updatable, default `now()` |
| `updated_at` | TIMESTAMPTZ | — |

#### LifeStat (`life_stats` table)
| Column | Type | Constraints |
|---|---|---|
| `id` | UUID | PK, auto-generated |
| `user_id` | UUID | not null, FK → users |
| `category` | VARCHAR(30) | not null, enum STRING |
| `value` | INTEGER | not null, 1–100 |
| `previous_value` | INTEGER | nullable |
| `last_updated` | TIMESTAMPTZ | not null, default `now()` |
| `metadata` | TEXT | optional JSON |

Unique constraint: `(user_id, category)`

#### LifeStatHistory (`life_stat_history` table)
| Column | Type | Constraints |
|---|---|---|
| `id` | UUID | PK, auto-generated |
| `user_id` | UUID | not null, FK → users |
| `category` | VARCHAR(30) | not null, enum STRING |
| `old_value` | INTEGER | not null |
| `new_value` | INTEGER | not null |
| `reason` | TEXT | optional |
| `changed_at` | TIMESTAMPTZ | not null |

### Enums

#### StatCategory
Values: `CAREER`, `WEALTH`, `FITNESS`, `EDUCATION`, `SOCIAL_INFLUENCE`

#### RankTier
Values: `BRONZE`, `SILVER`, `GOLD`, `PLATINUM`, `ELITE`

#### QuestStatus
Values: `ACTIVE`, `EXPIRED`, `ARCHIVED`

#### FriendRequestStatus
Values: `PENDING`, `ACCEPTED`, `REJECTED`

#### AchievementCategory
Values: `STATS`, `SOCIAL`, `STREAK`, `MILESTONE`, `SPECIAL`

#### ActivityFrequency
Values: `DAILY`, `WEEKLY`

#### FeedEventType
Values: `ACTIVITY`, `LEVEL_UP`, `ACHIEVEMENT`, `STREAK`

#### CosmeticCategory
Values: `PROFILE_THEME`, `TITLE`, `BADGE_FRAME`, `AVATAR_SKIN`, `GLOW_EFFECT`, `BANNER`

#### CosmeticRarity
Values: `COMMON`, `RARE`, `EPIC`, `LEGENDARY`

#### Quest (`quests` table)
| Column | Type | Constraints |
|---|---|---|
| `id` | UUID | PK, auto-generated |
| `title` | VARCHAR(200) | not null |
| `description` | TEXT | nullable |
| `xp_reward` | INTEGER | not null |
| `category` | VARCHAR(30) | not null, enum (StatCategory) |
| `status` | VARCHAR(20) | not null, default `'ACTIVE'` |
| `deadline` | TIMESTAMPTZ | nullable |
| `created_at` | TIMESTAMPTZ | not null, default `now()` |

#### QuestCompletion (`quest_completions` table)
| Column | Type | Constraints |
|---|---|---|
| `id` | UUID | PK, auto-generated |
| `user_id` | UUID | not null, FK → users |
| `quest_id` | UUID | not null, FK → quests |
| `completed_at` | TIMESTAMPTZ | not null, default `now()` |

Unique constraint: `(user_id, quest_id)` — prevents double completion

#### FriendRequest (`friend_requests` table)
| Column | Type | Constraints |
|---|---|---|
| `id` | UUID | PK, auto-generated |
| `sender_id` | UUID | not null, FK → users |
| `receiver_id` | UUID | not null, FK → users |
| `status` | VARCHAR(20) | not null, default `'PENDING'`, enum STRING |
| `created_at` | TIMESTAMPTZ | not null, default `now()` |
| `updated_at` | TIMESTAMPTZ | nullable |

Constraint: `sender_id <> receiver_id`

#### Friendship (`friendships` table)
| Column | Type | Constraints |
|---|---|---|
| `id` | UUID | PK, auto-generated |
| `user_id` | UUID | not null, FK → users |
| `friend_id` | UUID | not null, FK → users |
| `created_at` | TIMESTAMPTZ | not null, default `now()` |

Unique constraint: `(user_id, friend_id)`. Bidirectional: 2 rows per friendship.

#### LeaderboardSnapshot (`leaderboard_snapshots` table)
| Column | Type | Constraints |
|---|---|---|
| `id` | UUID | PK, auto-generated |
| `user_id` | UUID | not null, FK → users |
| `friend_owner_id` | UUID | not null, FK → users |
| `position` | INTEGER | not null |
| `score` | BIGINT | not null |
| `snapshot_date` | DATE | not null |
| `category` | VARCHAR(30) | nullable, enum (StatCategory) |

#### ActivityType (`activity_types` table)
| Column | Type | Constraints |
|---|---|---|
| `id` | UUID | PK, auto-generated |
| `name` | VARCHAR(100) | not null |
| `description` | TEXT | nullable |
| `icon` | VARCHAR(50) | nullable |
| `category` | VARCHAR(30) | not null, enum (StatCategory) |
| `xp_reward` | INTEGER | not null |
| `frequency` | VARCHAR(20) | not null, default `'DAILY'` |
| `cooldown_hours` | INTEGER | not null, default `24` |
| `is_active` | BOOLEAN | not null, default `true` |
| `created_at` | TIMESTAMPTZ | not null, default `now()` |

#### ActivityLog (`activity_log` table)
| Column | Type | Constraints |
|---|---|---|
| `id` | UUID | PK, auto-generated |
| `user_id` | UUID | not null, FK → users |
| `activity_type_id` | UUID | not null, FK → activity_types |
| `xp_earned` | INTEGER | not null |
| `logged_at` | TIMESTAMPTZ | not null, default `now()` |

Index: `(user_id, activity_type_id, logged_at)` for cooldown checks

#### UserStreak (`user_streaks` table)
| Column | Type | Constraints |
|---|---|---|
| `id` | UUID | PK, auto-generated |
| `user_id` | UUID | not null, unique, FK → users |
| `current_streak` | INTEGER | not null, default `0` |
| `longest_streak` | INTEGER | not null, default `0` |
| `last_activity_date` | DATE | nullable |

#### FeedEvent (`feed_events` table)
| Column | Type | Constraints |
|---|---|---|
| `id` | UUID | PK, auto-generated |
| `user_id` | UUID | not null, FK → users |
| `event_type` | VARCHAR(30) | not null, enum (FeedEventType) |
| `event_data` | TEXT | JSON-serialized event data |
| `created_at` | TIMESTAMPTZ | not null, default `now()` |

#### CosmeticItem (`cosmetic_items` table)
| Column | Type | Constraints |
|---|---|---|
| `id` | UUID | PK, auto-generated |
| `name` | VARCHAR(100) | not null |
| `description` | TEXT | nullable |
| `preview_url` | VARCHAR(500) | nullable |
| `category` | VARCHAR(30) | not null, enum (CosmeticCategory) |
| `price` | INTEGER | not null |
| `rarity` | VARCHAR(20) | not null, default `'COMMON'` |
| `is_active` | BOOLEAN | not null, default `true` |
| `created_at` | TIMESTAMPTZ | not null, default `now()` |

#### UserCosmetic (`user_cosmetics` table)
| Column | Type | Constraints |
|---|---|---|
| `id` | UUID | PK, auto-generated |
| `user_id` | UUID | not null, FK → users |
| `cosmetic_item_id` | UUID | not null, FK → cosmetic_items |
| `purchased_at` | TIMESTAMPTZ | not null, default `now()` |
| `is_equipped` | BOOLEAN | not null, default `false` |

Unique constraint: `(user_id, cosmetic_item_id)`

### Social Entities (model-only, NOT yet wired)

> [!WARNING]
> These entities have JPA mappings but NO services, controllers, or repositories.

| Entity | Table | Key Relationships |
|---|---|---|
| `Post` | `posts` | `@ManyToOne User`, has `caption`, `mediaUrl`, `mediaType` |
| `Comment` | `comments` | `@ManyToOne Post`, `@ManyToOne User`, has `text` |
| `Like` | `likes` | `@ManyToOne Post`, `@ManyToOne User` |
| `Follow` | `follows` | `@EmbeddedId FollowId`, `@ManyToOne follower`, `@ManyToOne following` |
| `FollowId` | — | Composite key: `followerId` + `followingId` |
| `Point` | `points` | `@OneToOne User` (maps `user_id` as PK), has `points`, `lastUpdated` |

## Repositories

| Repository | Entity | Custom Methods |
|---|---|---|
| `UserRepository` | User | `findByUsername`, `findByEmail`, `existsByUsername`, `existsByUsernameIgnoreCase`, `existsByEmail`, `findAllByOrderByTotalScoreDesc(Pageable)`, `countByTotalScoreGreaterThan(long)`, `searchByUsernameOrFullName(query, excludeUserId, Pageable)` |
| `LifeStatRepository` | LifeStat | `findAllByUserId`, `findByUserIdAndCategory`, `findAllByUserIdInAndCategory(List<UUID>, StatCategory)` |
| `LifeStatHistoryRepository` | LifeStatHistory | `findAllByUserIdOrderByChangedAtDesc`, `findAllByUserIdAndCategoryOrderByChangedAtDesc` |
| `QuestRepository` | Quest | `findAllByStatus(QuestStatus)`, `findByIdAndStatus(UUID, QuestStatus)` |
| `QuestCompletionRepository` | QuestCompletion | `findAllByUserId(UUID)`, `existsByUserIdAndQuestId(UUID, UUID)` |
| `FriendRequestRepository` | FriendRequest | `findAllByReceiverIdAndStatus`, `findAllBySenderIdAndStatus`, `countByReceiverIdAndStatus`, `existsBySenderIdAndReceiverIdAndStatus`, `findBySenderIdAndReceiverId` |
| `FriendshipRepository` | Friendship | `findAllByUserId(Pageable)`, `existsByUserIdAndFriendId`, `deleteByUserIdAndFriendId`, `searchFriends(userId, query)`, `findFriendIdsByUserId(userId)` |
| `LeaderboardSnapshotRepository` | LeaderboardSnapshot | `findByFriendOwnerIdAndSnapshotDate`, `findByFriendOwnerIdAndSnapshotDateAndCategory` |
| `ActivityTypeRepository` | ActivityType | `findAllByIsActiveTrue`, `findAllByCategoryAndIsActiveTrue`, `findByIdAndIsActiveTrue` |
| `ActivityLogRepository` | ActivityLog | `findAllByUserIdOrderByLoggedAtDesc(Pageable)`, `findAllByUserIdAndLoggedAtBetween`, `findFirstByUserIdAndActivityTypeIdAndLoggedAtAfter...` |
| `UserStreakRepository` | UserStreak | `findByUserId` |
| `FeedEventRepository` | FeedEvent | `findFriendFeed(userId, Pageable)` — JPQL join with Friendship |
| `CosmeticItemRepository` | CosmeticItem | `findAllByIsActiveTrue`, `findAllByIsActiveTrueAndCategory` |
| `UserCosmeticRepository` | UserCosmetic | `findAllByUserId`, `findByUserIdAndCosmeticItemId`, `existsByUserIdAndCosmeticItemId` |

## Flyway Migrations

Located in `src/main/resources/db/migration/`:

| Migration | Description |
|---|---|
| `V1__add_profile_completion_fields.sql` | Adds `profile_complete`, `updated_at` to users; constrains `username` to VARCHAR(20) unique; adds index |
| `V2__create_life_stats_table.sql` | Creates `life_stats` table with unique `(user_id, category)` constraint |
| `V3__add_previous_value_and_history.sql` | Adds `previous_value` to `life_stats`; creates `life_stat_history` table |
| `V4__add_total_score_to_users.sql` | Adds `total_score BIGINT DEFAULT 0` to users |
| `V5__add_rank_tier_to_users.sql` | Adds `rank_tier VARCHAR(20) DEFAULT 'BRONZE'` to users |
| `V6__create_quests_tables.sql` | Creates `quests` and `quest_completions` tables with indexes |
| `V7__create_friend_request_table.sql` | Creates `friend_requests` table with indexes and constraints |
| `V8__create_friendship_table.sql` | Creates `friendships` table with unique constraint and indexes |
| `V9__create_leaderboard_snapshots_table.sql` | Creates `leaderboard_snapshots` table with composite indexes |
| `V10__create_achievements_table.sql` | Creates `achievements` master table with code unique constraint |
| `V11__create_user_achievements_table.sql` | Creates `user_achievements` join table with unique(user_id, achievement_id) |
| `V12__add_displayed_achievements_to_users.sql` | Creates `user_displayed_achievements` @ElementCollection table |
| `V13__seed_initial_achievements.sql` | Seeds 12 default achievements across 5 categories |
| `V14__create_activity_types_table.sql` | Creates `activity_types` table with indexes |
| `V15__create_activity_log_table.sql` | Creates `activity_log` table with composite index |
| `V16__add_xp_level_to_users.sql` | Adds `xp` (BIGINT) and `level` (INT) to users |
| `V17__create_user_streaks_table.sql` | Creates `user_streaks` table with unique user_id |
| `V18__create_feed_events_table.sql` | Creates `feed_events` table with indexes |
| `V19__create_cosmetic_tables.sql` | Creates `cosmetic_items` + `user_cosmetics` tables |
| `V20__seed_activity_types.sql` | Seeds 25 activity types across 5 stat categories |
| `V21__add_structured_onboarding_fields.sql` | Adds verification/fallback columns to `user_occupation`, `user_wealth`, `user_wisdom` (structured onboarding redesign) |
| `V22__add_profile_cosmetic_and_ranking_fields.sql` | Adds `world_rank`, `country_rank`, `country`, vote score, active cosmetic columns to `users` |
| `V23__create_profile_votes_table.sql` | Creates `profile_votes` table (per-user upvote/downvote on profiles) |
| `V24__seed_frame_and_background_cosmetics.sql` | Adds `BACKGROUND`/`FRAME` cosmetic categories, seeds default free items |

**Next migration should be named**: `V25__<description>.sql`

> [!NOTE]
> **Two parallel migration histories exist.** `src/main/resources/db/migration/` (Flyway) is what
> Hibernate validates against in every environment (`ddl-auto=validate` in prod) and what CI/tests run.
> `supabase/migrations/` (Supabase CLI, applied via `supabase start`/`supabase db push`) contains a
> largely-duplicated history — e.g. `V21__add_structured_onboarding_fields.sql` and
> `supabase/migrations/20260503115343_add_structured.sql` are the same change under two names. When
> adding a migration, check whether it needs to be mirrored into `supabase/migrations/` too. The
> `bookmark` table has **no migration in either location** — see `docs/known-issues.md`.

### Structured onboarding stats (current model — supersedes the `life_stats`/`StatCategory` model below)

Populated via the `registration/` multi-step draft → commit flow, one dedicated table per
`shared.domain.model.StatDomain`. Scoring reads these, not `life_stats`.

| Table | Entity | Key columns |
|---|---|---|
| `user_occupation` | `UserOccupation` | `user_id`, `job_title`, `raw_job_title`, `company_name`, `raw_company_name`, `is_verified`, `industry`, `employment_type` (enum) |
| `user_wealth` | `UserWealth` | `user_id` (unique), `income_bracket`, `income_verified`, `net_worth_bracket`, `monthly_spending_bracket`, `monthly_saving_pct`, `income_currency`, `score` |
| `user_physique` | `UserPhysique` | `user_id` (unique), `height_cm`, `weight_kg`, `body_fat_pct`, `fitness_level` (enum), `workout_source`, `weekly_workouts`, `score` |
| `user_wisdom` | `UserWisdom` | `user_id` (unique), `hobbies`/habitIds (jsonb), `languages` (jsonb), `education_level`, `institution_name`, `graduation_year`, `score` |
| `user_charisma` | `UserCharisma` | `user_id` (unique), `onnyth_profile_likes`, `score`, `last_social_sync_at`, `relationship_status`, `social_circle_size` |
| `user_social_accounts` | `UserSocialAccount` | unique `(user_id, platform)`, `platform` (enum `SocialPlatform`), `username`, `profile_url`, `follower_count`, `is_verified`, `verified_at` |
| `user_xfactors` | `UserXfactor` | `user_id`, `type` (enum `XfactorType`), `title`, `description`, `evidence_url`, `metric_value`, `metric_label`, `is_verified` |
| `profile_likes` | `ProfileLike` | composite key (`ProfileLikeId`: liker/target) |
| `registration_drafts` | `RegistrationDraftEntity` | `user_id` (PK), `current_step` (enum `RegistrationStep`, default `PHONE`), `draft_data` (jsonb), `version`, `expires_at` (default now()+30d) |

### `life_stats` / `life_stat_history` (orphaned — superseded, do not use for new work)

> [!WARNING]
> These tables and the `StatCategory` enum they used (`CAREER`, `WEALTH`, `FITNESS`, `EDUCATION`,
> `SOCIAL_INFLUENCE`) are **no longer written by any current application code** — no `LifeStat` /
> `LifeStatHistory` domain classes exist anymore. They were replaced by the structured per-domain stat
> tables above. The tables still exist in the schema (created by `V2`/`V3`, never dropped). See
> `docs/known-issues.md` §2. `.agents/skills/life-stats/SKILL.md` still describes this old model and
> needs regeneration — do not trust its file paths or endpoints.

## Database Configuration

```properties
spring.datasource.url=${DATABASE_URL}           # jdbc:postgresql://...
spring.datasource.username=${DATABASE_USERNAME}
spring.datasource.password=${DATABASE_PASSWORD}
spring.jpa.hibernate.ddl-auto=validate           # Schema managed by Flyway only
spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect
```

## Adding New Entities

1. Create the JPA entity class in `models/`
2. Create a Flyway migration `V{N}__description.sql` in `src/main/resources/db/migration/`
3. Create a Spring Data JPA repository in `repository/`
4. Use `@Builder`, `@Getter`, `@Setter`, `@NoArgsConstructor`, `@AllArgsConstructor` from Lombok
5. Use `UUID` for all primary keys with `@GeneratedValue(strategy = GenerationType.AUTO)`
