# Known Issues & Documentation/Implementation Mismatches

Concrete, verified gaps between docs, code, tests, and product backlog. Fix the doc side when safe;
flag code-side issues here rather than silently "fixing" them as part of an unrelated task.

## 1. `bookmark` table has no schema migration (real production risk)

**Problem**: `BookmarkEntity` (`bookmark/adapter/out/persistence/BookmarkEntity.java`) maps to table
`bookmark`, but no `src/main/resources/db/migration/V*.sql` or `supabase/migrations/*.sql` file creates
it.

**Why it's not caught by tests**: `src/test/resources/application-test.properties` sets
`spring.jpa.hibernate.ddl-auto=update` (deliberately, to survive Testcontainers teardown), so Hibernate
silently creates the table for every test run. Production (`application.properties`) sets
`spring.jpa.hibernate.ddl-auto=validate`, which requires the table to already exist.

**Impact**: deploying current `main` to a fresh database (or one without a manually-run `CREATE TABLE
bookmark ...`) will fail Hibernate schema validation at startup for the bookmark feature.

**Desired behavior**: a `V{next}__create_bookmark_table.sql` migration matching `BookmarkEntity`'s
columns/constraints.

**Why it hasn't been fixed**: not determined from the repository — likely an oversight during rapid
feature development; not flagged by CI because CI's test profile also uses `ddl-auto=update`.

## 2. `.agents/skills/life-stats/SKILL.md` describes a superseded data model

**Problem**: The skill describes a generic `StatCategory` enum (`CAREER`, `WEALTH`, `FITNESS`,
`EDUCATION`, `SOCIAL_INFLUENCE`) with `LifeStat`/`LifeStatHistory` entities and a `service/`,
`controller/`, `repository/` package layout. None of these classes exist in
`src/main/java/com/onnyth/onnythserver/lifestats/` anymore.

**Current reality**: `lifestats/` now holds domain-specific entities —`UserOccupation`, `UserWealth`,
`UserPhysique`, `UserWisdom`, `UserCharisma`, `UserSocialAccount`, `UserXfactor`, `ProfileLike` — each
with its own port/adapter, populated through the `registration/` multi-step onboarding flow
(`RegistrationDraft` → commit). Scoring now keys off `shared.domain.model.StatDomain`
(`OCCUPATION`/`WEALTH`/`PHYSIQUE`/`WISDOM`/`CHARISMA`, each with a weight), not `StatCategory`.
`achievement/application/AchievementProgressCalculator.java` contains an explicit comment: "Updated to
use the new domain-specific stat repositories instead of LifeStat."

**Impact**: an agent trusting `life-stats/SKILL.md` will reference nonexistent classes/endpoints and
misunderstand the current scoring model.

**Status**: flagged, not rewritten here. `life-stats/SKILL.md` needs a full regeneration from the
`lifestats/` + `registration/` source, not a path-only patch, because the business model itself
changed (not just file locations). Recommended follow-up task.

**Orphaned schema**: the `life_stats` and `life_stat_history` tables (created by `V2`/`V3` migrations)
still exist in the schema (no migration drops them) but are no longer written by any current
application code.

## 3. Several skills reference pre-hexagonal-migration file paths

`authentication`, `error-handling`, and `testing` skills were written before the hexagonal migration
(see ADR-0001) and reference paths like `service/SupabaseAuthService.java`,
`controller/AuthController.java`, `exceptions/handler/GlobalExceptionHandler.java`. The *behavior*
they describe (auth flows, error response shape, test types) is still accurate; only the file paths
are wrong. These have been corrected in this pass — verify against source if they drift again.

## 4. PITest mutation testing configuration is stale and matches almost no current code

**Problem**: `pom.xml`'s `pitest-maven` `targetClasses` (`com.onnyth.onnythserver.service.*`,
`controller.*`, `exceptions.handler.*`) and `targetTests` (`com.onnyth.onnythserver.unit.*`,
`controller.*`) reference packages that no longer exist post-migration (verified empty via `find`).
`models.*` still resolves, but only to the 3 unwired `Post`/`Comment`/`Like` placeholders.

**Impact**: `./mvnw pitest:mutationCoverage` runs without error but currently exercises essentially none
of the real feature code (use cases, controllers, exception handlers all moved to per-feature
`application.usecase` / `adapter.in.rest` / `shared.exception` packages).

**Desired behavior**: `targetClasses`/`targetTests` updated to per-feature patterns, e.g.
`com.onnyth.onnythserver.*.application.usecase.*`, `com.onnyth.onnythserver.*.adapter.in.rest.*`,
`com.onnyth.onnythserver.shared.exception.*`.

**Why it hasn't been fixed**: not determined from the repository — the hexagonal migration commits
did not touch `pom.xml`'s PITest configuration. Not caught by CI because a "0 mutations analyzed" run
still exits successfully (`mutationThreshold=0`).

## 5. `.agents/prd/product-backlog.md` status drift

The backlog marks "Global leaderboard", "Category-specific leaderboards", "User search/discovery" as
`💡 IDEA` (Epic 5) despite `leaderboard/` and `search/` being fully implemented, tested, and documented
in `api-reference/SKILL.md`. Friend requests/friendships and achievements are implemented but have no
corresponding backlog epic at all. The sprint folders (`.agents/prd/sprints/sprint-3` through
`sprint-7`) show this work did happen; the backlog master list was not updated per the "after sprint
completion" step in `.agents/prd/ORCHESTRATOR.md`.

## 6. `Follow` is persistence-only, not user-facing

`friendship/domain/model/Follow.java` and its persistence adapter exist and are wired to a hexagonal
port, but no `application/usecase` or REST endpoint uses them. Don't assume follow/unfollow works
end-to-end just because the port/adapter look complete.

## 7. Bookmarks are not scoped to a user

`Bookmark`/`BookmarkEntity` has no `userId`/owner field, and `BookmarkController` never reads the JWT
subject. All `/api/v1/bookmarks/**` endpoints require *a* valid Supabase JWT (per `SecurityConfig`'s
`anyRequest().authenticated()`), but any authenticated user can read, update, or delete any other
user's bookmark. Confirm with the product spec ("UC-1" referenced in commit `f8f2e93`) whether
bookmarks are intentionally global/shared or this is a missing ownership check before building more on
top of it.

## 8. Two parallel, manually-synced migration histories

`src/main/resources/db/migration/` (Flyway, authoritative for `ddl-auto=validate` in every real
environment) and `supabase/migrations/` (Supabase CLI, used by `supabase start`/`supabase db push` for
local Postgres bootstrap) contain a largely-duplicated history — e.g. Flyway's
`V21__add_structured_onboarding_fields.sql` and `supabase/migrations/20260503115343_add_structured.sql`
are the same change, kept in sync manually. This is presumably how the `bookmark` table migration was
dropped (issue §1): it's easy to add a migration in one location and forget the other, or forget both.
No tooling enforces the two histories stay in sync.

## 9. Redis not provisioned in `docker-compose.yml`

`bookmark`'s idempotency guarantee (ADR-0002) depends on Redis (`spring.data.redis.host=localhost`,
port `6379`), but `docker-compose.yml` only starts `kafka` and `app`. A contributor running only
`docker compose up` will have bookmark creation silently fall back to "no idempotency" (best-effort
fail-open, per ADR-0002) rather than an obvious error.
