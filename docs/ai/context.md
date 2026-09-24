# AI Development Context

> This file is the **entry point for AI-assisted development** on Onnyth Server. Read it before
> making any code change. It links out to deeper context instead of duplicating it — follow the links
> relevant to the task at hand, don't load the entire `docs/` tree for every change.

## Before touching code

1. Read `/AGENTS.md` (repo root) if you haven't already this session — it's the durable, always-loaded
   entry point and links here.
2. Read `docs/README.md` for the full documentation map.
3. Read the specific `docs/features/<feature>.md` for the feature you're changing.
4. Read `docs/architecture/dependency-rules.md` if your change crosses a feature boundary or touches
   more than one layer (domain/application/adapter).
5. Check `docs/decisions/README.md` for any ADR governing the area you're touching — do not propose an
   alternative to an established, ADR-recorded decision without calling that out explicitly.
6. Check `docs/development/known-issues.md` and `docs/development/current-state.md` — you may be about
   to touch code with a documented, known gap; don't silently "fix" it as a side effect of an unrelated
   task (see `docs/ai/development-rules.md`).

## What each part of `docs/` is for

| Directory | Answers |
|---|---|
| `product/` | What Onnyth is and why, in domain terms — not implementation |
| `architecture/` | How the system is structured and the rules for extending it |
| `features/` | What the server currently does, feature by feature — the most-used reference |
| `api/` | The external HTTP contract: conventions, errors, OpenAPI |
| `data/` | The database schema, entities, relationships, and consistency model |
| `engineering/` | Coding standards, testing, security, performance, observability, deployment |
| `decisions/` | Why a hard-to-reverse architectural choice was made (ADRs) |
| `development/` | The current, verified implementation state — what's done, in progress, broken, planned |
| `ai/` | This directory — rules for how an AI agent should work in this repository |

## The single most important rule

**Documentation must reflect the current implementation, not an idealized or planned one.** Every
`docs/features/*.md` file has a `## Status` field (`Implemented` / `Partially Implemented` / `Planned`
/ `Deprecated`) — trust it, and update it if your change moves a feature from one status to another.
See `docs/ai/documentation-rules.md` for the full synchronization protocol.

## Related documents

- `docs/ai/development-rules.md` — the step-by-step contract for implementing a change
- `docs/ai/architecture-rules.md` — architectural boundaries an AI must not silently cross
- `docs/ai/documentation-rules.md` — what to update, and when, after a code change
