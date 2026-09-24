# Vision

> This document captures the product's stated direction as embodied by what has actually been built.
> No separate vision/mission statement exists in this repository beyond the shipped feature set itself
> — this is a synthesis, not a quotation of an authored vision statement. Treat it as directionally
> accurate, not as a verbatim source.

## The bet

Real-world self-improvement (career growth, financial health, fitness, education, social
relationships) is slow, unquantified, and hard to stay motivated about. Games are good at making
slow progress feel rewarding through visible scores, ranks, levels, streaks, and social comparison.
Onnyth applies that toolkit to real life instead of a fictional one.

## What "winning" looks like for a user

- Their real-world stats (occupation, wealth, physique, wisdom, charisma) are recorded once through a
  guided onboarding flow, then updated as their life changes.
- Their overall score, rank, and level rise as a direct, visible function of that real-world
  progress plus consistent engagement (streaks, quests, activities).
- They can see how they compare to friends (leaderboards, profile comparison), not just against an
  abstract global scale.
- Progress is rewarded with things worth showing off (achievements/badges, cosmetics, a currency to
  spend on cosmetics) — not just a bigger number.

## Product priorities implied by build order

The order features were built in (see git history and
`docs/decisions/ADR-0001-hexagonal-architecture.md`) suggests the following priority: (1) get a real
user account + real-world stat data in reliably (auth, profile,
onboarding), (2) make that data feel like a game (scoring, ranking), (3) prove out gamification
mutual-reinforcement mechanics (quests, achievements, activities, XP/leveling, streaks, cosmetics),
(4) layer social comparison on top (friendships, leaderboards, feed, search) once the individual loop
existed. A fifth, not-yet-built layer (posts/comments/likes/follow/notifications — see
`docs/development/future-work.md`) would extend this into a fuller social network.

## Non-goals (as evidenced by the codebase)

- This is not a general social network today — there is no content-posting feature live (see
  `models/Post`/`Comment`/`Like` as unwired placeholders in
  `docs/architecture/module-structure.md`).
- This is not a monetization-first product yet — `store/` exists for cosmetics, but there is no
  evidence of a real-money payment integration; `onnythCoins` is an in-app currency, not a payment
  processor integration.
