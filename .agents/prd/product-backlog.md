# Onnyth Server — Product Backlog

> This is the master list of all features, epics, and improvements. Items are pulled from here into sprint plans.

## Status Legend

| Status | Meaning |
|---|---|
| ✅ DONE | Shipped and tested |
| 🔄 IN_PROGRESS | Currently in a sprint |
| 📋 PLANNED | Scoped, ready for sprint |
| 💡 IDEA | Needs scoping before sprint |

---

## Epic 1: Core Platform (✅ DONE)

| # | Feature | Status | Sprint |
|---|---|---|---|
| 1.1 | Supabase Auth (signup, login, refresh, logout) | ✅ DONE | Sprint 1 |
| 1.2 | User model + profile CRUD | ✅ DONE | Sprint 1 |
| 1.3 | Profile picture upload (Supabase Storage) | ✅ DONE | Sprint 1 |
| 1.4 | Profile completion logic | ✅ DONE | Sprint 1 |
| 1.5 | Username availability check | ✅ DONE | Sprint 1 |

## Epic 2: Life Stats & Gamification (✅ DONE)

| # | Feature | Status | Sprint |
|---|---|---|---|
| 2.1 | Life stats CRUD (5 categories) | ✅ DONE | Sprint 2 |
| 2.2 | Bulk stat input (onboarding) | ✅ DONE | Sprint 2 |
| 2.3 | Stat update with history tracking | ✅ DONE | Sprint 2 |
| 2.4 | Weighted score calculation + event-driven recalc | ✅ DONE | Sprint 2 |
| 2.5 | 5-tier rank system (BRONZE → ELITE) | ✅ DONE | Sprint 2 |
| 2.6 | Profile card with rank data | ✅ DONE | Sprint 2 |
| 2.7 | Rank progress endpoint | ✅ DONE | Sprint 2 |

## Epic 3: Testing & Quality (✅ DONE)

| # | Feature | Status | Sprint |
|---|---|---|---|
| 3.1 | Unit tests for all services, DTOs, models | ✅ DONE | Sprint 2 |
| 3.2 | Controller tests (WebMvcTest) | ✅ DONE | Sprint 2 |
| 3.3 | Integration test with Testcontainers + WireMock | ✅ DONE | Sprint 2 |
| 3.4 | Repository tests with Testcontainers | ✅ DONE | Sprint 2 |
| 3.5 | PITest mutation testing integration | ✅ DONE | Sprint 2 |

## Epic 4: Social Features (💡 IDEA)

> Models exist (`Post`, `Comment`, `Like`, `Follow`, `Point`) but no services/controllers/repos.

| # | Feature | Status | Sprint |
|---|---|---|---|
| 4.1 | Post CRUD (create, list, delete) | 💡 IDEA | — |
| 4.2 | Comments on posts | 💡 IDEA | — |
| 4.3 | Like/unlike posts | 💡 IDEA | — |
| 4.4 | Follow/unfollow users | 💡 IDEA | — |
| 4.5 | Activity feed | ✅ DONE | Sprint 7 |
| 4.6 | Points system | 💡 IDEA | — |

## Epic 5: Leaderboard & Discovery (💡 IDEA)

| # | Feature | Status | Sprint |
|---|---|---|---|
| 5.1 | Global leaderboard by total score | 💡 IDEA | — |
| 5.2 | Category-specific leaderboards | 💡 IDEA | — |
| 5.3 | User search / discovery | 💡 IDEA | — |
| 5.4 | Trending users | 💡 IDEA | — |

## Epic 6: Notifications & Engagement (💡 IDEA)

| # | Feature | Status | Sprint |
|---|---|---|---|
| 6.1 | Push notification infrastructure | 💡 IDEA | — |
| 6.2 | Rank-up notifications | 💡 IDEA | — |
| 6.3 | Follow notifications | 💡 IDEA | — |
| 6.4 | Weekly stat reminders | 💡 IDEA | — |

## Epic 7: Activity & Progression (✅ DONE)

| # | Feature | Status | Sprint |
|---|---|---|---|
| 7.1 | Activity types catalog (25 types across 5 categories) | ✅ DONE | Sprint 7 |
| 7.2 | Activity logging with cooldown validation | ✅ DONE | Sprint 7 |
| 7.3 | XP system (separate from totalScore) | ✅ DONE | Sprint 7 |
| 7.4 | Level system with progressive XP curve + titles | ✅ DONE | Sprint 7 |
| 7.5 | Daily streak tracking with milestones | ✅ DONE | Sprint 7 |
| 7.6 | Profile card extended with level, title, streak | ✅ DONE | Sprint 7 |
| 7.7 | Activity feed (friend feed with pagination) | ✅ DONE | Sprint 7 |
| 7.8 | Cosmetic store (browse, purchase, equip, inventory) | ✅ DONE | Sprint 7 |

---

## How to Use This Backlog

1. When planning a new sprint, review items with status `📋 PLANNED` or `💡 IDEA`
2. Move items to `📋 PLANNED` once scoped
3. Create a sprint folder + sprint plan in `.agents/prd/sprints/`
4. After completing a sprint, update statuses to `✅ DONE` with the sprint number
