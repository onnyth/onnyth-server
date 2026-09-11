# Sprint Plan — Sprint 3: Pre-Work — Leaderboards & Quests

> **Goal**: Build the Leaderboard and Quest backend systems in parallel with client Sprint 001 to unblock future client sprints
> **Status**: `COMPLETED`
> **Source**: [Backend Brief](backend-brief.md)
> **Client Sprint**: sprint-001
> **Source PRD**: PRD-001 — Bottom Tab Navigation & Core Screen Scaffolding

## Context

Client Sprint 001 is **100% client-side** (tab navigation restructuring) — no blocking backend work. The brief recommends using this window to get ahead on features the client will need soon. This sprint focuses on the 🔴 HIGH priority pre-work items.

## Sprint Scope

| # | Story | Client Stories | Priority | Status |
|---|---|---|---|---|
| S3-01 | Leaderboard System | S-006 (future Leaderboards tab) | 🔴 Must | ✅ DONE |
| S3-02 | Quest System | S-006 (future Quests tab) | 🔴 Must | ✅ DONE |

## Stories

- [x] [S3-01 — Leaderboard System](stories/S3-01--leaderboard-system.md)
- [x] [S3-02 — Quest System](stories/S3-02--quest-system.md)

## Deferred to Later Sprints (from brief §5)

| Item | Priority | Reason |
|---|---|---|
| User Search (`GET /api/v1/users/search`) | 🟡 Medium | Client won't need until Sprint 3–4 |
| Follow/Unfollow (`UserFollow` entity) | 🟢 Low | Client won't need until social features sprint |

## Ordering Rationale

1. **S3-01 (Leaderboards)** first — simpler data model (query-based, no CRUD), quick win
2. **S3-02 (Quests)** second — more complex (full CRUD + completion tracking + XP awards)

## Sprint Notes

_Record any lessons, blockers, or observations here after sprint completion._
