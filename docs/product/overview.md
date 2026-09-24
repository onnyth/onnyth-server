# Onnyth — Product Overview

## What Onnyth is

Onnyth is a **gamified life-tracking platform**. A user builds an "Onnyth" profile representing their
real-world life across five structured domains — occupation, wealth, physique, wisdom, and charisma —
and progresses through an RPG-style system of scores, ranks, levels, streaks, quests, and achievements
built on top of that real-world data. The platform layers a social system (friendships, leaderboards,
an activity feed, profile comparison, peer voting) on top of individual progression.

This repository is the **backend only** — a single Spring Boot REST API. The client (mobile/web app)
that consumes this API lives elsewhere.

## The core loop

```text
1. Onboard → structured multi-step draft (registration) → commit real-world stats (lifestats)
2. Stats produce a weighted score per domain and an overall total score (scoring)
3. Score maps to a rank tier (ranking) and drives a separate level/XP track (leveling, xp)
4. Daily engagement is reinforced by streaks, activity logging, and quests
5. Progress unlocks achievements/badges and spendable currency for cosmetics (store)
6. Progress is shared and compared socially — friends, leaderboards, feed, profile voting
```

## Why this exists

The product bet is that people are more motivated to develop real-world skills, career, fitness,
finances, and social relationships when that progress is made visible, quantified, and comparable —
borrowing engagement mechanics from RPGs and applying them to actual life outcomes rather than
in-game ones. See `docs/product/vision.md` for the fuller framing.

## What's implemented vs. planned

This document describes the product's intended shape. For what is *actually* built and working today
versus planned/idea-stage, see `docs/development/current-state.md` (the verified, code-checked
snapshot) — do not treat this file as a status report.

## Related documents

- `docs/product/core-concepts.md` — the domain concepts (User, StatDomain, Rank, Level, Quest,
  Achievement, Cosmetic, Friendship, Leaderboard, …) and what each means
- `docs/product/terminology.md` — terms that are easy to conflate
- `docs/product/users-and-roles.md` — who interacts with the system
- `docs/development/current-state.md` — verified implementation status
