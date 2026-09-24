# Future Work

Work that is scoped as an idea/plan but not yet implemented, or evident from feature docs as a natural
next step. This is a synthesis of each feature's own "Future Work" section and the known gaps in
`docs/development/known-issues.md` — check both directly before starting work, since this file can
drift.

## Not yet implemented

- **Post/Comment/Like CRUD** — `models/` has JPA entities only, no service/controller/repository.
- **Follow/unfollow as a real feature** — the persistence layer already exists (see
  `docs/features/friendships.md`, known issue #9); this is "finish wiring an existing model," not
  "build from scratch."
- **Points system** — no `Point`-related use case exists beyond an unwired placeholder model.
- **Push notifications** (rank-up, follow, weekly stat reminders) — no notification infrastructure of
  any kind exists in the codebase today.
- **Trending users** — no discovery/ranking-by-recent-activity logic exists.

## Natural next steps implied by known issues (not formally scoped, but low-ambiguity)

These aren't new feature ideas — they're finishing work already partially done, documented in detail
in `docs/development/known-issues.md`:

- Wire `StatChangedEvent`/`recalculateAll` so score recalculation actually happens after a stat change.
- Wire rank-tier refresh to run whenever `totalScore` changes, not only from quest completion.
- Wire `createFeedEvent(...)` calls into `activity/`, `leveling/`, `achievement/`, `streak/`.
- Broaden achievement re-evaluation triggers beyond friend-request acceptance.
- Fix the seed-data/enum drift in `V13`, `V20`, `V24`.
- Add the missing Flyway migrations (known issue #1).

## Once Follow becomes real

Follow notifications explicitly depend on follow/unfollow existing as a real application flow first —
don't scope follow notifications before follow/unfollow itself is built.

## Documentation-adjacent future work

- Consider a real load/performance-testing setup (`docs/engineering/performance.md` notes none exists).
- Consider a metrics backend + structured logging + tracing if production debugging needs improve
  beyond `docs/engineering/observability.md`'s current baseline.
