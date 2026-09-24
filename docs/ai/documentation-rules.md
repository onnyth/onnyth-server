# Documentation Synchronization Rules

Documentation is part of the implementation, not an afterthought. Whenever code changes, determine
whether documentation is affected using the table below, then update it in the **same** change — not
as separate follow-up work.

## What to update, by kind of change

| Change | Update |
|---|---|
| Feature behavior, endpoint, business rule, or entity changed | The matching `docs/features/<feature>.md` — replace the affected section(s) in place |
| New feature added | New `docs/features/<feature>.md` (use the standard template — see any existing file for the shape) + add a row to `docs/features/README.md` + update `docs/development/current-state.md` |
| Feature removed/deprecated | Set that feature's `## Status` to `Deprecated`, explain why and since when; update `docs/features/README.md` and `docs/development/current-state.md` |
| API contract changed (new/changed endpoint, request/response shape) | The feature's `docs/features/<feature>.md` "API" section; `docs/api/conventions.md` only if the *convention itself* changed (rare) |
| Database schema changed | `docs/data/database-schema.md` (table inventory + migration ledger) and the owning feature's "Data Model" section |
| New architectural decision, or one that reverses a prior ADR | New `docs/decisions/ADR-{next}-{slug}.md` (mark the old one `Superseded by ADR-000X` if applicable) — see `docs/ai/architecture-rules.md` |
| Architecture/module structure changed (new feature module, new cross-cutting pattern) | `docs/architecture/module-structure.md` and/or `docs/architecture/high-level-design.md` |
| Testing approach changed | `docs/engineering/testing.md` |
| Security/auth model changed | `docs/engineering/security.md` and `docs/features/authentication.md` if relevant |
| Deployment/infra changed | `docs/architecture/infrastructure.md`, `docs/engineering/deployment.md` |
| A gap/inconsistency is discovered (not caused by your change) | Add it to `docs/development/known-issues.md` — do not silently fix it unless it's in scope (see `docs/ai/development-rules.md` #8) |
| A gap you just fixed | Remove or update the corresponding entry in `docs/development/known-issues.md` |

Pure refactors with no behavior/contract change do not require a documentation update.

## How to update — not append

- **Replace sections in place.** Don't append a "Update 2026-09-23" note to the bottom of a file —
  edit the relevant section directly so the file always reads as current-state truth.
- **Never document planned work as implemented.** Use the `## Status` field
  (`Implemented` / `Partially Implemented` / `Planned` / `Deprecated`) precisely. If you're building
  toward a feature but it isn't done, it's `Partially Implemented` with a note on what's missing —
  never `Implemented`.
- **Never rewrite ADR history.** An accepted ADR that's later reversed gets superseded by a new ADR
  (`Status: Superseded by ADR-000X`), not edited to look like it never happened.
- **Keep `docs/development/current-state.md` and `docs/features/README.md` in sync** with the
  individual feature docs — these are summaries, and summaries that drift from the detail they
  summarize are worse than no summary at all.
- **Cite file paths precisely.** Every claim in a feature doc should be traceable to a real file — if
  you can't point at the file, don't state it as fact.

## Never

- Claim planned/future functionality is currently implemented.
- Leave documentation describing removed/replaced architecture as if it were current (this is exactly
  what happened to the pre-consolidation `.agents/skills/life-stats/SKILL.md` — see
  `docs/development/known-issues.md` for the cautionary example).
- Silently delete a known-issue entry without either fixing the underlying problem or confirming it no
  longer applies.
