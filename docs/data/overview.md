# Data Layer Overview

PostgreSQL (hosted on Supabase) is the single system of record for every feature. Access is entirely
through Spring Data JPA/Hibernate; each feature owns its own entity/repository classes under its own
`adapter/out/persistence` package — there is no shared, top-level `models`/`repository` package for
active features (see `docs/architecture/module-structure.md`).

## Two migration histories — read this before touching schema

There are **two** migration mechanisms in this repository, and they are **not equivalent**:

1. **Flyway** (`src/main/resources/db/migration/V*.sql`) — run automatically by Spring Boot on
   startup against whatever `DATABASE_URL` points to. `spring.jpa.hibernate.ddl-auto=validate` means
   Hibernate never creates/alters tables itself — it only verifies the entities match whatever schema
   already exists.
2. **Supabase CLI migrations** (`supabase/migrations/*.sql`) — applied via `supabase start` (local) or
   `supabase db push` (remote) directly against the actual Supabase-hosted Postgres project.

**Verified fact**: `supabase/migrations/20260412183914_remote_schema.sql` is a full baseline schema
snapshot (`supabase db pull` output) that alone creates 29 of the ~33 real tables in this system —
including nearly every table backing the *current* structured-stats and scoring model
(`user_occupation`, `user_wealth`, `user_physique`, `user_wisdom`, `user_charisma`,
`user_social_accounts`, `user_xfactors`, `profile_likes`, `score_history`, `sport_medals`,
`user_education`). **None of these 11 tables — nor `registration_drafts`, nor `bookmark`/
`bookmark_tags` — have a corresponding Flyway migration.** Flyway's `V1`–`V24` series only ever
creates 17 tables (see `docs/data/database-schema.md`) and only ever *alters* the structured-stats
tables (`V21`, `V22`) — it never creates them.

**Practical consequence**: Flyway's migration set, applied alone to an empty database, would **not**
reconstruct enough schema for the application to pass Hibernate's `ddl-auto=validate` check. Every
real environment's schema exists because of the Supabase-side migrations (baseline + incremental),
not because of Flyway. Flyway has never been the sole source of truth for schema creation in this
project — see `docs/development/known-issues.md` for the full, evidence-based writeup and the concrete
list of affected tables.

**What this means for new work**: when adding a new entity, add a Flyway migration *and* a mirrored
Supabase migration (per the existing, if inconsistently followed, convention) — do not assume Flyway
alone is sufficient, and do not assume an entity without a Flyway migration is safe just because tests
pass (`ddl-auto=update` in the test profile hides this class of gap entirely).

## Related documents

- `docs/data/database-schema.md` — full table inventory, migration ledger, per-table migration status
- `docs/data/entities.md` — every JPA entity class, its table, and its owning feature
- `docs/data/relationships.md` — foreign-key relationships between tables
- `docs/data/data-consistency.md` — transactional boundaries, the one Spring-event flow, the one
  Kafka/Redis eventual-consistency flow
- `docs/development/known-issues.md` — the schema-migration gap, in full, with evidence
