# Features Index

Every feature module has a corresponding doc here, verified against source. `## Status` in each file is
the authoritative per-feature status — this table is a summary generated from those files; if it ever
looks out of sync with a linked doc, trust the linked doc and fix this table.

| Feature | Doc | Status |
|---|---|---|
| Authentication | [`authentication.md`](authentication.md) | ✅ Implemented |
| Users | [`users.md`](users.md) | ✅ Implemented |
| Profiles | [`profiles.md`](profiles.md) | ✅ Implemented |
| Registration / Onboarding | [`registration.md`](registration.md) | ✅ Implemented (see doc for a profile-completion bypass caveat) |
| Life Stats | [`life-stats.md`](life-stats.md) | 🚧 Partially Implemented — no dedicated use case/controller, written only via `registration/` |
| Scoring | [`scoring.md`](scoring.md) | 🚧 Partially Implemented — **recalculation is not wired to any trigger** |
| Ranking | [`ranking.md`](ranking.md) | 🚧 Partially Implemented — tier is not auto-refreshed when score changes; `ELITE` is unreachable from the current score formula |
| Leveling | [`leveling.md`](leveling.md) | ✅ Implemented |
| XP | [`xp.md`](xp.md) | ✅ Implemented (response under-reports XP/level on streak-milestone days — see doc) |
| Streak | [`streak.md`](streak.md) | 🚧 Partially Implemented — no feed/event hook on milestones |
| Friendships | [`friendships.md`](friendships.md) | 🚧 Partially Implemented — friend requests/friendships/comparison work; `Follow` is persistence-only |
| Leaderboard | [`leaderboard.md`](leaderboard.md) | ✅ Implemented (friends-scoped only — no global leaderboard) |
| Feed | [`feed.md`](feed.md) | 🚧 Partially Implemented — **nothing in the codebase ever creates a feed event** |
| Search | [`search.md`](search.md) | ✅ Implemented |
| Achievements | [`achievements.md`](achievements.md) | 🚧 Partially Implemented — unlock evaluation only triggers on friend-request acceptance; some seeded achievements can never unlock |
| Quests | [`quests.md`](quests.md) | ✅ Implemented (no seed data or admin endpoint — catalog starts empty) |
| Activities | [`activities.md`](activities.md) | 🚧 Partially Implemented — seeded catalog uses stale category names |
| Cosmetics | [`cosmetics.md`](cosmetics.md) | 🚧 Partially Implemented — currency/rarity/seed data inconsistencies, equip doesn't unequip prior item |
| Bookmarks | [`bookmarks.md`](bookmarks.md) | ✅ Implemented, with a deployment-blocking missing migration |

## Not yet a feature (see `docs/development/future-work.md`)

`Post`/`Comment`/`Like` (in `models/`) have JPA entities only — no service, controller, or repository.
Not documented as a feature here because there is nothing to describe beyond "the table exists."

## How this index is maintained

When a feature's `## Status` changes, update its row here in the same change — see
`docs/ai/documentation-rules.md`.
