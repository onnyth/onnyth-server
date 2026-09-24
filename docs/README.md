# Onnyth Server Documentation

## Purpose

This directory is the **single source of truth** for understanding, developing, and maintaining the
Onnyth server application — for humans and AI agents alike. It is written and kept current against the
actual codebase, not an idealized or planned architecture.

Start at [`/AGENTS.md`](../AGENTS.md) (repo root) if you haven't already — it's the durable, always-loaded
entry point. AI agents specifically should read [`ai/context.md`](ai/context.md) before modifying code.

## Documentation structure

| Directory | Answers |
|---|---|
| [`product/`](product/) | What Onnyth is and why it exists — domain concepts, terminology, vision |
| [`architecture/`](architecture/) | How the server is designed and the rules for extending it |
| [`features/`](features/) | What the server currently supports, feature by feature (start at [`features/README.md`](features/README.md)) |
| [`api/`](api/) | The external API contract: conventions, error format, OpenAPI |
| [`data/`](data/) | Database schema, entities, relationships, consistency model |
| [`engineering/`](engineering/) | Coding standards, testing, security, performance, observability, deployment |
| [`decisions/`](decisions/) | Architectural decisions and their rationale (ADRs) |
| [`development/`](development/) | Current implementation state, active work, known issues, technical debt, future work |
| [`ai/`](ai/) | Rules and context specifically for AI-assisted development |
| [`status.md`](status.md) | One-page dashboard of feature/architecture/infrastructure status |

## Documentation rules

- Documentation must reflect the **current implementation** — see each feature doc's `## Status`
  field (`Implemented` / `Partially Implemented` / `Planned` / `Deprecated`). Never document planned
  functionality as implemented.
- Architectural changes require updating the relevant `architecture/` document.
- New/changed features require updating the matching `features/*.md` (and `features/README.md`,
  `development/current-state.md` if the feature is new or its status changed).
- Significant architectural decisions get an ADR under `decisions/` — see `decisions/README.md` for
  when one is warranted.
- A concrete, verified gap in the code (not just "this could be improved") belongs in
  `development/known-issues.md`, not fixed silently as a side effect of unrelated work.
- Full protocol: [`ai/documentation-rules.md`](ai/documentation-rules.md).

## Tracking new work

There is no separate product-backlog/sprint-planning system in this repository — `docs/development/`
is where planned, active, and completed work is tracked:
- `docs/development/active-work.md` — what's in progress right now
- `docs/development/future-work.md` — scoped or idea-stage work not yet started
- `docs/development/known-issues.md` — verified gaps to fix
- `docs/development/current-state.md` — the overall implemented/partial/planned snapshot

When new work is planned or completed, update these files directly rather than tracking it in a
separate location.

## History: this replaces two prior documentation/process systems

Earlier, this repository split technical reference across `.agents/skills/*/SKILL.md` (per-feature
skills) and a much smaller `docs/`, and tracked product/process history separately under `.agents/prd/`
(product backlog, sprint plans, user stories, a sprint-execution orchestrator). Both systems have been
**retired and fully consolidated into this `docs/` tree** — neither `.agents/skills/` nor `.agents/prd/`
exists anymore. Several of the old skill files had drifted significantly from the actual code after the
hexagonal-architecture migration (see `docs/decisions/ADR-0001-hexagonal-architecture.md`) and a later
registration/lifestats rework — the old `life-stats` skill describing a fully-superseded data model was
the clearest example. Every `docs/features/*.md` file in this tree was freshly verified against source
at the time of consolidation. Do not recreate either system as a parallel process/documentation tree —
everything durable belongs in `docs/`.
