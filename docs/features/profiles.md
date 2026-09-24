# Profiles

## Status

Implemented

## Purpose

Owns everything about how a user's identity and gamified progress is *presented*: editable profile
fields, profile picture storage, the "OnnythID" profile card (score, rank, level, streak, domain
scores, cosmetics, social votes), and peer-to-peer profile upvotes/downvotes.

## User Capabilities

- View and update their own profile (username, full name, profile picture).
- Check whether a username is available before committing to it.
- Upload/replace a profile picture (stored in Supabase Storage).
- View their own or another user's "profile card" — a single enriched read-model combining identity,
  score, rank, level, streak, per-domain scores, world/country rank, vote score, and active cosmetics.
- Set a solid hex background color for their card (as an alternative to an equipped background cosmetic).
- View their own rank progress toward the next tier.
- Upvote, downvote, remove a vote, or check their own vote on another user's profile.

## Business Rules

- **Profile completion**: `User.checkAndUpdateProfileCompletion()` sets `profileComplete = true` only
  when `username`, `fullName`, and `profilePic` are all non-blank. Called after every profile update
  and after picture upload.
- **Username**: 3–20 chars, must be unique **case-insensitively** (`existsByUsernameIgnoreCase`);
  changing to the same username (case-insensitive) is a no-op, not a conflict.
- **Full name**: max 100 chars.
- **Profile picture upload**: allowed types JPEG/JPG/PNG/WebP, max 5MB
  (`spring.servlet.multipart.max-file-size=5MB`); old picture is deleted from Supabase Storage before
  the new one is uploaded; stored at `profile-pics/{userId}/{randomUUID}.{ext}`.
- **Background color**: must match `^#[0-9A-Fa-f]{6}$`; setting a solid color clears any equipped
  background cosmetic (`activeBackgroundCosmetic = null`) — the two are mutually exclusive in the UI.
- **Voting**: a user cannot vote on their own profile (400); a vote can be cast, changed
  (up→down or vice versa), or removed; `voteScore` on `User` is the net upvotes minus downvotes.
- **Viewer mode**: the public/viewer profile-card endpoint returns the same `ProfileCardResponse` as
  the owner's — the client, not the server, is expected to hide `onnythCoins` when rendering another
  user's card (see `ProfileController` comment on `getPublicProfileCard`).

## API

**Base**: `/api/v1/profile` — authenticated unless noted.

| Method | Path | Auth | Request | Response | Status |
|---|---|---|---|---|---|
| `GET` | `/api/v1/profile` | JWT | — | `ProfileResponse` | 200 |
| `PUT` | `/api/v1/profile` | JWT | `ProfileUpdateRequest { username?, fullName?, profilePic? }` | `ProfileResponse` | 200 / 409 (username taken) |
| `GET` | `/api/v1/profile/check-username/{username}` | JWT (optional principal) | — | `{ username, available }` | 200 |
| `POST` | `/api/v1/profile/picture` | JWT | `multipart/form-data` (`file`) | `ProfileResponse` | 200 |
| `GET` | `/api/v1/profile/card` | JWT | — | `ProfileCardResponse` (own) | 200 |
| `GET` | `/api/v1/profile/{userId}/card` | JWT | — | `ProfileCardResponse` (viewer mode) | 200 |
| `PUT` | `/api/v1/profile/background-color` | JWT | `{ "color": "#RRGGBB" }` | `ProfileCardResponse` | 200 / 400 |
| `GET` | `/api/v1/profile/rank` | JWT | — | `RankProgressResponse` | 200 |
| `GET` | `/api/v1/users/{userId}/card` | **Public** | — | `ProfileCardResponse` | 200 |
| `POST` | `/api/v1/votes` | JWT | `VoteRequest { targetUserId, isUpvote }` | `VoteResponse` | 200 / 400 (self-vote) |
| `DELETE` | `/api/v1/votes/{targetUserId}` | JWT | — | `VoteResponse` | 200 |
| `GET` | `/api/v1/votes/{targetUserId}` | JWT | — | `VoteResponse` (`myVote` null = no vote) | 200 |

Two independent routes return the same profile card shape: the authenticated
`/api/v1/profile/{userId}/card` and the public `/api/v1/users/{userId}/card`
(`SecurityConfig` permits `/api/v1/users/*/card`; the analogous `/api/v1/profile/**` path is **not**
publicly permitted, so `/api/v1/profile/{userId}/card` still requires a JWT despite being "viewer mode").

## Data Model

Profile fields live on the shared `users` table (see `docs/data/database-schema.md`); this feature owns
one additional table:

| Table | Entity | Key columns | Migration |
|---|---|---|---|
| `profile_votes` | `ProfileVoteEntity` | `voter_id`, `target_user_id`, `is_upvote`, unique `(voter_id, target_user_id)` | `V23__create_profile_votes_table.sql` |

`ProfileCardResponse` is an assembled read-model, not a table — it's built by
`ProfileUseCaseService.getProfileCard()` by querying `User` plus one row from each of five other
features' repositories (see Dependencies).

## Domain Logic

`ProfileCardResponse` aggregates, per request:
1. `User` (identity, totalScore, rankTier, level, onnythCoins, worldRank, countryRank, voteScore, active
   cosmetics, activeBackgroundColor) — from `user/`.
2. Current streak — from `streak.application.port.UserStreakRepository`.
3. Five `DomainScoreDto` entries (`OCCUPATION`, `WEALTH`, `PHYSIQUE`, `WISDOM`, `CHARISMA`), each with a
   raw `score`, a **profile-local, hardcoded label** (e.g. score ≥ 80 → "Executive" for occupation,
   "High Net Worth" for wealth, "Iconic" for charisma; physique/wisdom labels instead key off
   `FitnessLevel`/`educationLevel` strings), and a **profile-local, hardcoded rank badge emoji**
   (🥉 <20, 🥈 <45, 🥇 <70, 💎 <90, 👑 ≥90) — from `lifestats.application.port.{UserOccupationRepository,
   UserWealthRepository,UserPhysiqueRepository,UserWisdomRepository,UserCharismaRepository}`.
4. `levelTitle` — via `leveling.application.usecase.LevelUseCaseService.getTitle(user.getLevel())`.
5. `rankTier`/`rankBadgeUrl` — via `ranking.domain.model.RankTier` display name/emoji (this is the
   *account-wide* rank tier and badge, distinct from the five per-domain badge emojis in step 3, which
   use their own independent thresholds defined locally in `ProfileUseCaseService`).

`RankUseCaseService.getRankProgress()` (backing `GET /profile/rank`) is a separate call into
`ranking/` — see `docs/features/ranking.md`.

## Events

None published directly. Profile updates do not publish `StatChangedEvent` — only `lifestats`/
`registration` writes do (see `docs/features/scoring.md`).

## Dependencies

`profile/` has read/write dependencies on `user`, `lifestats` (five domain repositories),
`streak`, `leveling`, `ranking`, and `store` (indirectly, via `User.activeFrameCosmetic` /
`activeBackgroundCosmetic`) — making it one of the most cross-feature-coupled modules in the codebase.
It depends on Supabase Storage (`profile/adapter/out/storage/StorageServiceAdapter.java`) for picture
upload/delete.

## Key Files

| File | Role |
|---|---|
| `profile/application/usecase/ProfileUseCaseService.java` | Profile CRUD, picture upload, profile-card assembly, background color |
| `profile/application/usecase/ProfileVoteUseCaseService.java` | Cast/remove/get vote |
| `profile/adapter/in/rest/ProfileController.java` | `/api/v1/profile/**` |
| `profile/adapter/in/rest/ProfileVoteController.java` | `/api/v1/votes/**` |
| `profile/adapter/out/storage/StorageServiceAdapter.java` | Supabase Storage upload/delete |
| `profile/adapter/out/persistence/{ProfileVoteEntity,ProfileVoteJpaRepository,ProfileVotePersistenceMapper,ProfileVoteRepositoryAdapter}.java` | JPA persistence for `profile_votes` |
| `profile/adapter/in/rest/dto/{ProfileResponse,ProfileUpdateRequest,ProfileCardResponse,DomainScoreDto,VoteRequest,VoteResponse}.java` | DTOs |
| `profile/application/exception/FileUploadException.java` | 400 |
| `user/adapter/in/rest/UserController.java` | Also serves the public `/api/v1/users/{userId}/card` variant |

## Known Limitations

- The five per-domain rank badge emoji thresholds in `ProfileUseCaseService.rankBadgeForScore()` are
  independent from, and use different score scales than, the account-wide `RankTier` thresholds in
  `ranking/` — an agent modifying one must not assume it affects the other.
- `GET /api/v1/profile/{userId}/card` still requires a JWT even though it's conceptually a "viewer"
  endpoint; only the separate `/api/v1/users/{userId}/card` route is actually public.

## Future Work

None currently scoped.
