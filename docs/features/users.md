# Users

## Status

Implemented

## Purpose

Owns the core `User` aggregate — the single row every other feature (profile, scoring, ranking,
leveling, streaks, cosmetics, friendships, etc.) joins against — plus account deletion.

## User Capabilities

- Delete their own account (and all associated data).
- (Admin/internal) List, fetch, create, update, delete any user by id/email — these endpoints have no
  role check beyond being authenticated; see Known Limitations.
- Anyone (no auth) can fetch another user's public profile card by id.

## Business Rules

- `User.checkAndUpdateProfileCompletion()` is the single source of truth for `profileComplete`; see
  `docs/features/profiles.md`.
- Username uniqueness is case-insensitive (`existsByUsernameIgnoreCase`).
- Account deletion is permanent and exists specifically to satisfy the Apple App Store account-deletion
  requirement (see `AccountController`'s Javadoc/description).

## API

| Method | Path | Auth | Request | Response | Status |
|---|---|---|---|---|---|
| `DELETE` | `/api/v1/account` | JWT required | — | — | 204 |
| `GET` | `/api/users` | JWT required | — | `List<User>` | 200 |
| `GET` | `/api/users/{id}` | JWT required | — | `User` | 200 / 404 |
| `GET` | `/api/users/email/{email}` | JWT required | — | `User` | 200 / 404 |
| `POST` | `/api/users` | JWT required | `User` (raw JSON body) | `User` | 200 |
| `PUT` | `/api/users/{id}` | JWT required | `User` (raw JSON body) | `User` | 200 |
| `DELETE` | `/api/users/{id}` | JWT required | — | — | 204 |
| `GET` | `/api/v1/users/{userId}/card` | **Public** (`SecurityConfig` permits `/api/v1/users/*/card`) | — | `ProfileCardResponse` | 200 |

## Data Model

`users` table, `user/adapter/out/persistence/UserEntity.java`. Core columns established across several
migrations: `id` (UUID PK), `username` (unique, 3–20 chars), `email` (unique), `full_name`,
`profile_pic`, `email_verified`, `profile_complete` (`V1`), `total_score` (`V4`), `rank_tier` (`V5`),
`xp`, `level` (`V16`), plus later additions: `world_rank`, `country_rank`, `country`, `vote_score`,
active cosmetic FKs (`V22`), `phone`, `profile_type`, `onnyth_coins`, `displayed_achievements`
(`@ElementCollection`, `V12`). See `docs/data/database-schema.md` for the full column list.

## Domain Logic

`user/domain/model/User.java` is a framework-free POJO (mutable, `@Builder`) shared by reference across
many features — it directly references `ranking.domain.model.RankTier` and `store.domain.model.CosmeticItem`,
meaning `user` has a compile-time dependency on `ranking` and `store` domain models. `UserUseCaseService`
provides plain CRUD; `AccountDeletionUseCaseService` performs the actual cross-table account deletion
for `DELETE /api/v1/account`.

## Events

None published directly by this feature. `user` is a *consumer* of `StatChangedEvent` indirectly via
`scoring/` (which persists `totalScore` back onto `User`).

## Dependencies

Nearly every feature in the system depends on `user/application/port/UserRepository` to resolve a
`User` by id. `user/domain/model/User` itself depends on `ranking` (`RankTier`) and `store`
(`CosmeticItem`) domain models.

## Key Files

| File | Role |
|---|---|
| `user/domain/model/User.java` | Core domain aggregate (mutable POJO) |
| `user/application/usecase/UserUseCaseService.java` | CRUD used by `UserController` |
| `user/application/usecase/AccountDeletionUseCaseService.java` | Full account deletion |
| `user/application/port/UserRepository.java` | Outbound port implemented by the persistence adapter |
| `user/adapter/in/rest/UserController.java` | `/api/users/**` admin CRUD + public `/api/v1/users/{userId}/card` |
| `user/adapter/in/rest/AccountController.java` | `/api/v1/account` — self-service delete |
| `user/adapter/out/persistence/{UserEntity,UserJpaRepository,UserPersistenceMapper,UserRepositoryAdapter}.java` | JPA persistence for `users` table |
| `user/application/exception/{EmailAlreadyExistsException,UsernameAlreadyExistsException,UserNotFoundException}.java` | 409 / 409 / 404 |

## Known Limitations

- `/api/users/**` (list/get/create/update/delete any user) requires only *a* valid JWT — there is no
  admin-role check. Any authenticated user can list all users, fetch any user by email, or update/delete
  an arbitrary user by id. Confirm with the product owner whether this is intended as an internal/admin
  surface (and should be role-gated) before building a client that exposes it.
- `POST /api/users` / `PUT /api/users/{id}` accept the raw `User` domain object as the request body
  (not a dedicated DTO), so any field on `User` (including `totalScore`, `xp`, `rankTier`, cosmetics)
  is client-settable through this path — inconsistent with the validated, field-limited
  `ProfileUpdateRequest` used by `PUT /api/v1/profile`.

## Future Work

None currently scoped.
