# Core Concepts

The domain concepts every feature is built from, defined in product terms (not implementation detail
— see each concept's linked `docs/features/*.md` for how it's actually built).

| Concept | Meaning |
|---|---|
| **User** | A single person's account and the aggregate root for all their progress — identity, score, rank, level, coins, cosmetics. See `docs/features/users.md`. |
| **Profile** | The presentable surface of a `User` — editable fields (username, name, picture) plus the assembled "profile card" read-model (score, rank, level, streak, domain scores, cosmetics, votes). See `docs/features/profiles.md`. |
| **Registration / Onboarding** | The guided, multi-step flow a new user completes to provide their real-world stat data, held as a draft until committed. See `docs/features/registration.md`. |
| **Stat Domain** | One of the five real-world life categories Onnyth measures: **Occupation, Wealth, Physique, Wisdom, Charisma**. Each has its own weight in the overall score and its own dedicated data (job info, income, fitness metrics, education, social signals). See `docs/features/life-stats.md`. |
| **Score** | A single weighted number (`totalScore`) summarizing a user's overall real-world progress across all five stat domains. See `docs/features/scoring.md`. |
| **Rank / Rank Tier** | A named tier (e.g. Bronze → Elite) derived from `totalScore`, giving a user a badge and a sense of overall standing. Distinct from Level (below). See `docs/features/ranking.md`. |
| **Level / XP** | A separate progression track driven by *engagement* (activities, quests, streaks) rather than raw life-stat score — a user levels up by earning XP, not by improving their stats directly. See `docs/features/leveling.md`, `docs/features/xp.md`. |
| **Streak** | Consecutive-day engagement tracking, with milestones. See `docs/features/streak.md`. |
| **Quest** | A defined task a user can complete once for an XP reward. See `docs/features/quests.md`. |
| **Activity** | A repeatable, cataloged real-world action a user logs (e.g. a workout), subject to a cooldown, that awards XP. Distinct from a Quest (one-off) and an Achievement (milestone, not directly loggable). See `docs/features/activities.md`. |
| **Achievement / Badge** | A milestone unlocked automatically when a condition is met (not directly performed by the user like an Activity) — displayed on a profile card, up to 3 at a time. See `docs/features/achievements.md`. |
| **Cosmetic** | A purchasable, equippable visual customization (frame, background, theme, etc.) for a profile, bought with `onnythCoins`. See `docs/features/cosmetics.md`. |
| **Friendship** | A mutual, bidirectional social connection formed by a request/accept flow. Distinct from the unimplemented one-directional **Follow** concept. See `docs/features/friendships.md`. |
| **Leaderboard** | A ranking of users by score, scoped to a friend group (not a single global public ranking) — overall or per stat domain. See `docs/features/leaderboard.md`. |
| **Feed** | A chronological timeline of a user's friends' notable events (activities, level-ups, achievements, streaks). See `docs/features/feed.md`. |
| **Bookmark** | A standalone, non-gamification utility feature (save a URL + title + tags) — included primarily as the reference implementation for idempotent writes and domain events. See `docs/features/bookmarks.md`. |

## Concepts that sound similar but are not the same

See `docs/product/terminology.md` for a focused list of easy-to-confuse pairs (Score vs. Level, Rank
vs. Level, Activity vs. Quest vs. Achievement, Friend vs. Follow, Coin vs. Score).
