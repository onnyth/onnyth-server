# Sprint Plan — Sprint 4: Friends System

> **Goal**: Build the complete friendship backend — user search, friend requests, friends list, friend profiles with stat comparison
> **Status**: `DONE`
> **Source**: [Backend Brief](backend-brief.md)
> **Client Sprint**: sprint-002
> **Source PRD**: PRD-002 — Friends System

## Context

Client Sprint 2 is building the Friends System UI with **full mock mode** — no backend endpoints are blocking client work. Backend should prioritize entities + core friend operations (orders 1-4) to unblock real API integration in Sprint 3+.

> [!IMPORTANT]
> Brief specifies V6/V7 migrations, but V6 already exists (quests). Using **V7/V8** instead.

## Sprint Scope

| # | Story | Client Stories | Priority | Status |
|---|---|---|---|---|
| S4-01 | Data Model — FriendRequest + Friendship | All friend operations | 🔴 Must | ✅ |
| S4-02 | User Search | S-010 | 🔴 Must | ✅ |
| S4-03 | Friend Requests — Send/Accept/Reject | S-011, S-012, S-016 | 🔴 Must | ✅ |
| S4-04 | Friends List + Remove | S-013, S-014 | 🔴 Must | ✅ |
| S4-05 | Friend Profile + Stat Comparison | S-015 | 🔴 Must | ✅ |

## Stories

- [x] [S4-01 — Data Model](stories/S4-01--data-model.md)
- [x] [S4-02 — User Search](stories/S4-02--user-search.md)
- [x] [S4-03 — Friend Requests](stories/S4-03--friend-requests.md)
- [x] [S4-04 — Friends List](stories/S4-04--friends-list.md)
- [x] [S4-05 — Friend Profile](stories/S4-05--friend-profile.md)

## Ordering Rationale

1. **S4-01 (Data Model)** first — entities + migrations are foundation for everything else
2. **S4-02 (User Search)** second — needed to find users to send requests to
3. **S4-03 (Friend Requests)** third — core friend flow (send → accept/reject)
4. **S4-04 (Friends List)** fourth — view/search/remove friends
5. **S4-05 (Friend Profile)** last — depends on friendship + stats infrastructure

## Sprint Notes

- Migration numbering adjusted (V7/V8 instead of V6/V7) to avoid conflict with existing quests migration.
- 20 new tests added (10 service + 10 controller). All pass.
- Pre-existing Testcontainers/Docker failures (24 errors) are unrelated to this sprint.
