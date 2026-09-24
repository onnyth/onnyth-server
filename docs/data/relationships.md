# Relationships

Nearly every table in the system has a foreign key (explicit or implicit — see below) back to `users`.
There is no feature whose data is not ultimately scoped to a user.

## Relationship map (centered on `users`)

```text
users (1)
 ├── (1:1)  user_occupation, user_wealth, user_physique, user_wisdom, user_charisma  [lifestats]
 ├── (1:1)  registration_drafts                                                      [registration]
 ├── (1:1)  user_streaks                                                              [streak]
 ├── (1:1)  score_history (per-domain rows, not strictly 1:1 — see docs/features/scoring.md)
 ├── (1:N)  user_social_accounts, user_xfactors, sport_medals                        [lifestats/scoring]
 ├── (1:N)  activity_log                                                             [activity]
 ├── (1:N)  quest_completions                                                        [quest]
 ├── (1:N)  user_achievements                                                        [achievement]
 ├── (1:N)  user_cosmetics                                                           [store]
 ├── (1:N)  feed_events                                                              [feed]
 ├── (1:N)  leaderboard_snapshots (as both user_id and friend_owner_id)               [leaderboard]
 ├── (1:N)  bookmark                                                                 [bookmark — NOT actually FK'd, see below]
 ├── (N:N via friendships)     friendships.user_id ↔ friendships.friend_id (2 rows per pair) [friendship]
 ├── (N:N via friend_requests) friend_requests.sender_id → friend_requests.receiver_id       [friendship]
 ├── (N:N via follows)         follows.follower_id → follows.following_id (unwired)          [friendship]
 ├── (N:N via profile_likes)   profile_likes (liker → target)                        [lifestats]
 └── (N:N via profile_votes)   profile_votes.voter_id → profile_votes.target_user_id [profile]

quests (1) ──(1:N)── quest_completions (N:1)── users
achievements (1) ──(1:N)── user_achievements (N:1)── users
activity_types (1) ──(1:N)── activity_log (N:1)── users
cosmetic_items (1) ──(1:N)── user_cosmetics (N:1)── users
```

## Notable relationship details

- **`friendships` is bidirectional by row duplication**, not a single undirected edge: accepting a
  friend request writes two rows (`(A,B)` and `(B,A)`), each independently unique-constrained on
  `(user_id, friend_id)`. See `docs/features/friendships.md`.
- **`bookmark` has no `user_id` column at all** — it is not scoped to any user, despite requiring
  authentication to access. This is a documented gap, not a relationship to rely on. See
  `docs/features/bookmarks.md` and `docs/development/known-issues.md`.
- **`leaderboard_snapshots.friend_owner_id`** is distinct from `user_id` — a snapshot row represents
  "how `user_id` ranked within `friend_owner_id`'s friend group," not a global ranking. See
  `docs/features/leaderboard.md`.
- **`user_achievements` and `user_displayed_achievements`** are two different relationships: the
  former is every achievement a user has *unlocked*; the latter (an `@ElementCollection` of up to 3
  achievement ids directly on `User`) is which unlocked achievements the user has chosen to *display*
  on their profile card.
- **`User.activeFrameCosmetic` / `User.activeBackgroundCosmetic`** are direct object references to
  `CosmeticItem` (from `store/`) held on the `User` domain model — not a join table — meaning
  `user/domain/model/User.java` has a compile-time dependency on `store/domain/model/CosmeticItem`.

## Cross-feature dependency (not FK) relationships

Beyond database foreign keys, several features have a **compile-time port dependency** on another
feature without any shared table:

```text
profile   → user, lifestats (×5 repos), streak, leveling, ranking, store (via User)
feed      → activity, leveling, achievement, streak (as event sources — verify exact set in docs/features/feed.md)
leaderboard → friendship (friend graph), user (scores)
scoring   → lifestats (reads scores), user (persists totalScore)
ranking   → user (persists rankTier)
```

See `docs/architecture/low-level-design.md` for why this is expected under the hexagonal architecture
(a port dependency, not a raw repository/table dependency).
