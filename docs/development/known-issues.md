# Known Issues

Concrete, verified gaps between docs, code, tests, and product backlog — cross-checked against actual
source (not assumed). Fix the doc side when safe; flag code-side issues here rather than silently
"fixing" them as part of an unrelated task (see `docs/ai/development-rules.md` #8). Grouped by theme;
numbered for cross-referencing from feature docs.

## Schema / migration gaps

### 1. Roughly half the tables in the system have no Flyway migration

**Problem**: `src/main/resources/db/migration/` (Flyway — what every real environment validates
against, `ddl-auto=validate`) only ever creates 17 of ~33 tables. The rest exist solely because of
`supabase/migrations/20260412183914_remote_schema.sql` (a full `supabase db pull` baseline snapshot)
or a standalone Supabase-only migration. Affected tables, all currently backing **live, wired business
logic** (not placeholders): `bookmark`/`bookmark_tags` (no migration anywhere), `follows`,
`user_occupation`, `user_wealth`, `user_physique`, `user_wisdom`, `user_charisma`,
`user_social_accounts`, `user_xfactors`, `user_education`, `profile_likes`, `score_history`,
`sport_medals`, `registration_drafts`. See `docs/data/overview.md` and
`docs/data/database-schema.md` for the full table-by-table breakdown.

**Why it's not caught by tests**: `src/test/resources/application-test.properties` sets
`spring.jpa.hibernate.ddl-auto=update`, so Hibernate silently creates every table Testcontainers needs,
regardless of migration state.

**Impact**: deploying to a genuinely fresh database (Flyway-only bootstrap, no prior Supabase baseline
applied) would fail Hibernate schema validation for at least 13 tables' worth of entities.

**Desired behavior**: a Flyway migration for each of the tables above, matching what
`supabase/migrations/` already defines.

### 2. `feed_events.event_data` type mismatch between Flyway and the entity

Flyway's `V18__create_feed_events_table.sql` defines `event_data` as `TEXT`; the JPA entity
(`FeedEventEntity`) maps it as JSON (`@JdbcTypeCode(SqlTypes.JSON)`, `columnDefinition = "jsonb"`); the
Supabase baseline snapshot uses `JSONB`. Flyway and JPA disagree with each other; Supabase and JPA agree.
See `docs/features/feed.md`.

### 3. (Resolved by docs consolidation) Product-backlog status drift

Previously, a separate `.agents/prd/product-backlog.md` marked "Global leaderboard",
"Category-specific leaderboards", and "User search/discovery" as `💡 IDEA` despite `leaderboard/` and
`search/` being fully implemented and tested, and had no entry at all for friendships/achievements
despite both being shipped. That backlog (and the rest of `.agents/prd/`) has since been retired —
`docs/features/README.md` and `docs/development/current-state.md` are now the single, actively
maintained source for feature status, so this specific drift can no longer recur. Retained here as a
numbered slot (not renumbering the list below) and as a concrete example of why a status summary that
isn't the single source of truth tends to drift — see `docs/ai/documentation-rules.md`.

## Unwired code paths (exist, but nothing calls them)

### 4. Score recalculation is not triggered by anything

`scoring/application/usecase/ScoreCalculationUseCaseService.recalculateAll(...)` has **no call sites**
anywhere in the current source. `shared/domain/event/StatChangedEvent` is defined but never published
by `lifestats/` or `registration/`, and `scoring/` declares no `@EventListener` for it. In practice, a
user's five domain scores and `totalScore` are only ever what `recalculateAll` would compute if it ran
— today they default to `0`/unchanged after registration commits. See `docs/features/scoring.md` and
`docs/features/life-stats.md`.

### 5. Rank tier is not auto-refreshed when score changes

`ranking/application/usecase/RankUseCaseService.updateUserRank(...)` is only called from
`quest/application/usecase/QuestUseCaseService.java`. Score changes from registration or (if #4 is
ever fixed) scoring recalculation do not automatically update the persisted `rank_tier`. See
`docs/features/ranking.md`.

### 6. Feed events are never created

`feed/application/usecase/FeedUseCaseService.createFeedEvent(...)` has no call sites anywhere in the
codebase. `activity/`, `leveling/`, `achievement/`, and `streak/` are the intended producers (per
Sprint 7's story `S7-07--activity-feed.md`) but none of them call it. The friend-feed read API, table,
and query all work correctly — the feed is simply always empty in practice. See
`docs/features/feed.md`.

### 7. Achievement unlock evaluation only runs on friend-request acceptance

`achievement/application/usecase/AchievementUnlockUseCaseService` is called from exactly one place:
`friendship/application/usecase/FriendshipUseCaseService` after a friend request is accepted. Stat
changes, score changes, profile completion, level-ups, and streak updates never trigger an achievement
re-evaluation. See `docs/features/achievements.md`.

### 8. `LevelUpEvent` has no listeners

`leveling/` publishes a `LevelUpEvent`, but no listener in the current source consumes it (no feed
item, no notification, no achievement check). See `docs/features/leveling.md` and finding #6 above —
this is the same "publishes but nothing subscribes" shape as the feed gap.

### 9. `Follow` is persistence-only, not user-facing

`friendship/domain/model/Follow.java` and its persistence adapter exist and are wired to a hexagonal
port, but `FollowRepository` exposes only read-side count/existence methods — there is no save/delete
port method, no `application/usecase` logic, and no REST endpoint. Don't assume follow/unfollow works
end-to-end just because the port/adapter look complete. The backing `follows` table also has no Flyway
migration (see #1). See `docs/features/friendships.md`.

### 10. `registration_drafts` cleanup is dead code

`RegistrationDraftRepository.deleteExpiredDrafts(...)` exists but no scheduler or startup hook calls
it — expired onboarding drafts accumulate indefinitely. See `docs/features/registration.md`.

## Data model / seed data inconsistencies

### 11. Seeded catalogs use pre-migration category names

`V13__seed_initial_achievements.sql` seeds `STAT_VALUE_CAREER`/`STAT_VALUE_FITNESS` and
`UPDATE_STREAK`-type achievements; `V20__seed_activity_types.sql` seeds activity types under
`FITNESS`/`EDUCATION`/`SOCIAL_INFLUENCE`/`CAREER`/`WEALTH`. None of these match the current
`shared.domain.model.StatDomain` enum (`OCCUPATION`/`WEALTH`/`PHYSIQUE`/`WISDOM`/`CHARISMA`) that
`AchievementProgressCalculator` and the activity controller actually key off. Concretely: the two
seeded `STAT_VALUE_*` achievements never progress, and `AchievementProgressCalculator` hard-codes
`case "UPDATE_STREAK" -> 0`, so both seeded streak achievements are permanently locked at 0%. See
`docs/features/achievements.md` and `docs/features/activities.md`.

### 12. `RANK_GOLD` achievement threshold is miscalibrated

The seeded `RANK_GOLD` achievement uses threshold `3`, but `calculateRankTierProgress()` computes
`rankTier.ordinal() * 100 / threshold`. With the current 5-tier `RankTier` ordering, this formula
reaches 100% at `PLATINUM` (ordinal 3), not `GOLD` (ordinal 2) as the achievement name implies. See
`docs/features/achievements.md`.

### 13. Cosmetic store currency/rarity/category drift

`StoreController` and Sprint 7's story doc (`S7-08--cosmetic-store.md`) describe purchases as
XP-based, but `StoreUseCaseService` actually spends `users.onnyth_coins`. No code anywhere awards
`onnythCoins` to a user, and no Flyway migration creates the `onnyth_coins` column — only the Supabase
schema snapshot has it. Separately, `V24__seed_frame_and_background_cosmetics.sql` seeds a rarity value
`FREE`, but `CosmeticRarity` only defines `COMMON`/`RARE`/`EPIC`/`LEGENDARY`. Only `BACKGROUND`/`FRAME`
items are seeded — the other four `CosmeticCategory` values have no catalog entries. See
`docs/features/cosmetics.md`.

### 14. Cosmetic equip doesn't unequip the previous item

`StoreUseCaseService.equipItem()` never unequips a previously-equipped item in the same category, so
multiple `user_cosmetics` rows can end up `is_equipped = true` simultaneously. See
`docs/features/cosmetics.md`.

### 15. Registration bypasses the real profile-completion check

`RegistrationCommitUseCaseService` unconditionally sets `user.profileComplete = true` on commit, even
though the actual onboarding flow only strictly requires the `PHONE` and `NAME` steps — this bypasses
`User.checkAndUpdateProfileCompletion()`'s username/fullName/profilePic check entirely. See
`docs/features/registration.md`.

### 16. Charisma scoring reads `profile_likes`, not the user-facing `profile_votes`

`scoring/`'s charisma formula counts rows in `profile_likes` (a `lifestats/`-owned table with no
write path exposed to users), while the actual user-facing "vote on a profile" API
(`ProfileVoteController`) writes to a separate table, `profile_votes`. These are two different tables
that sound related but are not connected. See `docs/features/life-stats.md` and
`docs/features/scoring.md`.

## Response-accuracy issues

### 17. XP/level responses under-report on streak-milestone days

`ActivityLogResponse.newTotalXP`/`newLevel`/`levelTitle` are captured **before**
`StreakUseCaseService.recordActivity(...)` runs and potentially awards milestone bonus XP (which can
itself trigger a second level-up). On a milestone day, the API response under-reports the user's true
final XP/level until the next read. See `docs/features/xp.md` and `docs/features/streak.md`.

### 18. Quest completion response can show a stale rank tier

`QuestUseCaseService.completeQuest()` builds `QuestCompletionResponse.rankTier` from the `User` object
fetched *before* calling `RankUseCaseService.updateUserRank(...)`, so a tier-up triggered by the same
quest completion isn't reflected in that response. See `docs/features/quests.md`.

## Security / authorization gaps

### 19. No role-based authorization anywhere

`/api/users/**` (list/get/update/delete any user) and `/api/v1/bookmarks/**` (any bookmark) are
reachable by any authenticated user with no ownership or role check. See
`docs/engineering/security.md`, `docs/features/users.md`, `docs/features/bookmarks.md`.

### 20. `search` endpoints require auth despite being reference-data lookups

`/api/v1/search/{roles,companies,universities,languages}` require a JWT because
`SecurityConfig` ends with `anyRequest().authenticated()` and doesn't whitelist them, even though the
underlying data is hard-coded reference data with no per-user sensitivity. See
`docs/features/search.md`.

## Test / tooling gaps

### 21. PITest mutation-testing configuration is stale and matches almost no current code

`pom.xml`'s `pitest-maven` `targetClasses` (`com.onnyth.onnythserver.service.*`, `controller.*`,
`exceptions.handler.*`) and `targetTests` (`unit.*`, `controller.*`) reference packages that no longer
exist post-hexagonal-migration; `models.*` still resolves but only to the three unwired
`Post`/`Comment`/`Like` placeholders, which have no tests. `./mvnw pitest:mutationCoverage` runs
successfully but analyzes effectively none of the real feature code, and is not wired into
`.github/workflows/ci-cd.yml` at all. See `docs/engineering/testing.md`.

### 22. Leftover empty pre-migration test directories

`src/test/java/com/onnyth/onnythserver/{controller,dto,models,repository}/` are empty leftovers from
before the hexagonal migration. Do not add new tests there.

## Infrastructure gaps

### 23. Redis not provisioned in `docker-compose.yml`

`bookmark`'s idempotency guarantee depends on Redis (`localhost:6379`), but `docker-compose.yml` only
starts `kafka` and `app`. A contributor running only `docker compose up` gets silent
"no idempotency" (fail-open, per ADR-0002) rather than an obvious error.

### 24. `bookmark` is not scoped to a user

`Bookmark`/`BookmarkEntity` has no `userId`/owner field, and `BookmarkController` never reads the JWT
subject. Any authenticated user can read, update, or delete any other user's bookmark. Confirm with the
product spec ("UC-1", commit `f8f2e93`) whether this is intentionally global/shared. See
`docs/features/bookmarks.md`.

## Documentation history (context for future agents)

This `docs/` tree was consolidated from two prior systems: a per-feature skills tree
(`.agents/skills/*/SKILL.md`) and a product/process tree (`.agents/prd/` — product backlog, sprint
plans, a sprint-execution orchestrator), alongside a much smaller original `docs/`. Both prior systems
have been retired; do not recreate either as a parallel system — see `docs/README.md`. The old
`.agents/skills/life-stats/SKILL.md` in particular described a `StatCategory`/`LifeStat` model that had
already been fully replaced by the structured per-domain tables described in
`docs/features/life-stats.md` — a concrete example of why `docs/ai/documentation-rules.md` insists on
replacing sections in place rather than leaving superseded documentation to accumulate.
