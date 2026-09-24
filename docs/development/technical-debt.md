# Technical Debt

Intentional or accumulated compromises worth fixing deliberately, distinct from
`docs/development/known-issues.md` (which is the evidence-based, itemized list this file summarizes
into themes). See that file for full detail and file-level citations on every item referenced here.

## 1. Two schema-migration histories, one of them incomplete

Flyway (`src/main/resources/db/migration/`) is treated as the schema source of truth by
`ddl-auto=validate`, but roughly half the system's tables were only ever created via
`supabase/migrations/` (or a single baseline dump). This is the single highest-value piece of debt to
pay down: a new Flyway migration per missing table, verified against the Supabase-side definition. See
known issues #1–#2.

## 2. "Publishes/exposes a hook, but nothing consumes it" is a recurring pattern

Four independent features have this exact shape: `StatChangedEvent` (scoring), `createFeedEvent(...)`
(feed), `LevelUpEvent` (leveling), and achievement re-evaluation (only wired from one trigger point).
Each was very likely intended to be wired more broadly (evidenced by Sprint 7's own story docs
describing the intended cross-feature wiring) but the wiring step was never completed. Fixing all four
is largely the same shape of work: add the missing publish call or `@EventListener`/direct call at each
real trigger point (stat commit, activity log, level-up, quest/achievement/streak changes). See known
issues #4, #6, #7, #8.

## 3. Seed data drift from current enums

`V13` (achievements), `V20` (activity types), and `V24` (cosmetics) all seed rows using category/type
names that predate a later enum rename or model change (`StatCategory` → `StatDomain`, missing
`CosmeticRarity.FREE`). This silently breaks specific catalog entries rather than causing a startup
failure, which is why it went unnoticed. See known issues #11–#13.

## 4. PITest is configured for a package layout that no longer exists

`pom.xml`'s `pitest-maven` plugin references pre-hexagonal-migration packages. It runs without error
(so nothing failed loudly) but analyzes essentially none of the real feature code today. See known
issue #21.

## 5. No role-based authorization model

Two endpoints groups (`/api/users/**`, `/api/v1/bookmarks/**`) are reachable by any authenticated user
with no ownership/role check. Introducing a real authorization model would be an architectural
decision (new ADR), not a quick fix — see `docs/ai/architecture-rules.md`.

## Prioritization note

This list intentionally does not assign priority — that's a product/engineering-lead call, not
something to infer from how this document is ordered. Use `docs/development/known-issues.md`'s
per-item "Impact" framing plus current product priorities to decide what to schedule.
