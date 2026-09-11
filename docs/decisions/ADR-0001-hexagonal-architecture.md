# ADR-0001: Per-feature Hexagonal Architecture

## Status

Accepted (superseded the prior top-level layered architecture — see Context).

## Context

The codebase originally used a single top-level layered structure: `controller/`, `service/`,
`repository/`, `models/`, `exceptions/` at the root of `com.onnyth.onnythserver`. Git history shows a
deliberate, sequential migration away from this, one module at a time:

```
1270d3a refactor: migrate shared foundation and user module to hexagonal architecture
567e556 refactor(profile): migrate profile module to hexagonal architecture
bebcc5d refactor(auth): migrate auth module to hexagonal architecture
940e6b8 refactor(lifestats): migrate life-stats domain models to hexagonal architecture
89cd7f0 refactor(scoring-ranking): migrate scoring, ranking, leveling, xp, and streak into hexagonal architecture
c942d84 refactor(activity): migrate activity module to hexagonal architecture
d795179 refactor(friendship): migrate friendship module to hexagonal architecture
4dc8568 refactor(leaderboard): migrate leaderboard module to hexagonal architecture
a801a0a refactor(search): migrate search module to hexagonal architecture
0a714a4 refactor(achievement): migrate achievement module to hexagonal architecture
7c7cfac refactor(quest): migrate quest module to hexagonal architecture
11e3b01 refactor(store): migrate store/cosmetics module to hexagonal architecture
3a8b4c8 refactor(feed): migrate feed module to hexagonal architecture
55af33c refactor(cleanup): finish hexagonal migration — registration module, shared kernel, legacy package removal
```

The historical rationale (why hexagonal specifically, vs. e.g. a simpler layered-by-feature split) is
**not documented** in the repository. `.agents/skills/conventions/SKILL.md` states the resulting rule
but not the motivation. Do not fabricate a justification beyond what is verifiable here.

## Decision

Every feature module is organized as a self-contained hexagon:

```text
{feature}/
├── domain/model, domain/event
├── application/port, application/usecase, application/exception
└── adapter/in/rest (+ adapter/in/kafka), adapter/out/persistence (+ adapter/out/kafka)
```

Dependency direction is strictly `adapter → application → domain`. Cross-cutting concerns
(`ApiException`, `StatDomain`, idempotency contracts) live in `shared`, not in any one feature.

## Alternatives Considered

Not documented. The only verifiable prior state is the original flat
`controller/service/repository/models` layout, which this decision replaced entirely (all modules were
migrated; none were left on the old structure by design — `models/Post`, `Comment`, `Like` remain only
as unwired social-feature placeholders, not as an example of the old architecture in active use).

## Why This Decision

Not documented beyond the stated goal in `.agents/skills/conventions/SKILL.md`: enforce a consistent
per-feature boundary and keep framework/persistence concerns out of domain and application code.

## Consequences

- **Easier**: locating all code for a feature (one package subtree); replacing an adapter (e.g. swap
  persistence tech) without touching application/domain code; unit-testing use cases without Spring.
- **Harder**: some technical docs (`.agents/skills/authentication`, `error-handling`, `testing`,
  `api-reference`) still reference pre-migration file paths (`controller/`, `service/`, `models/`,
  `exceptions/handler/`) — see `docs/known-issues.md`. New agents must verify paths against source
  before trusting a skill's "Key Files" table.
- Cross-feature reuse must go through a feature's `application/port`, not its entity/repository —
  slightly more ceremony for simple lookups, but keeps persistence details from leaking across
  feature boundaries.

## Implementation

See any feature module under `src/main/java/com/onnyth/onnythserver/` (e.g. `bookmark/`, `auth/`,
`profile/`) and `.agents/skills/conventions/SKILL.md` for the canonical shape.

## Related

- `.agents/skills/conventions/SKILL.md`, `.agents/skills/onnyth-overview/SKILL.md`
- `docs/known-issues.md` (stale skill file paths)
