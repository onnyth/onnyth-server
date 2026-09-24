# Search

## Status

Implemented

## Purpose

This feature provides two distinct search surfaces: user discovery and onboarding/reference autocomplete. User discovery searches the real `users` table and annotates each result with friendship/request state, while the onboarding endpoints serve curated in-memory lists for roles, companies, universities, and languages.

## User Capabilities

- Search other users by username or full name.
- See whether a returned user is already a friend or has a pending request.
- Autocomplete job roles, company names, and university names during onboarding/profile flows.
- Fetch the bundled language list.

## Business Rules

- `GET /api/v1/users/search` performs a case-insensitive substring match on `username` and `fullName`.
- User search always excludes the authenticated user (`u.id <> :excludeUserId`).
- User search annotates each result with `isFriend` and `requestPending`, where `requestPending` checks both directions for a `PENDING` request.
- User search defaults to `page=0`, `size=20`, and caps `size` at 50.
- User search does **not** reject blank `q`; because the repository query is `LIKE %:query%`, `q=` effectively becomes a paginated browse of all other users.
- The onboarding/reference endpoints are fully in-memory: data is loaded once at startup by `SearchUseCaseService.init()` and no DB query or external HTTP call is made.
- `searchRoles()`, `searchCompanies()`, and `searchUniversities()` return `[]` for blank/whitespace queries.
- Those three autocomplete endpoints return at most 8 results, prioritizing prefix matches before fallback substring matches.
- `GET /api/v1/search/languages` returns the full static `{code, name}` list with no server-side filtering.

## API

| Method | Path | Auth | Request | Response | Status |
|---|---|---|---|---|---|
| `GET` | `/api/v1/users/search` | JWT required | Query `q`, `page`, `size` | `Page<UserSearchResponse>` (`userId`, `username`, `fullName`, `profilePic`, `rankTier`, `isFriend`, `requestPending`) | `200` |
| `GET` | `/api/v1/search/roles` | JWT required | Query `q` | `List<String>` | `200` |
| `GET` | `/api/v1/search/companies` | JWT required | Query `q` | `List<String>` | `200` |
| `GET` | `/api/v1/search/universities` | JWT required | Query `q` | `List<String>` | `200` |
| `GET` | `/api/v1/search/languages` | JWT required | — | `List<LanguageEntry>` (`code`, `name`) | `200` |

## Data Model

| Table / source | Entity / owner | Key columns / values | Constraints / source |
|---|---|---|---|
| `users` | `user/adapter/out/persistence/UserEntity.java` | `id`, `username`, `full_name`, `profile_pic`, `rank_tier` | Backing store for `/api/v1/users/search`; searched via `user/adapter/out/persistence/UserJpaRepository.java` |
| `friendships` | `friendship/adapter/out/persistence/FriendshipEntity.java` | `user_id`, `friend_id` | Used to derive `isFriend`; created by `src/main/resources/db/migration/V8__create_friendship_table.sql` |
| `friend_requests` | `friendship/adapter/out/persistence/FriendRequestEntity.java` | `sender_id`, `receiver_id`, `status` | Used to derive `requestPending`; created by `src/main/resources/db/migration/V7__create_friend_request_table.sql` |
| In-memory datasets | `search/application/usecase/SearchUseCaseService.java` | Roles, companies, universities, languages | Hard-coded Java lists loaded at startup; no table or migration |

## Domain Logic

`search/application/usecase/UserSearchUseCaseService.java` runs the actual `users` query through `user/adapter/out/persistence/UserJpaRepository.java`, then checks `friendship/` and `friend_requests` for each hit to attach social state. `search/application/usecase/SearchUseCaseService.java` is separate: it builds four curated datasets inside the service, stores them in memory during `@PostConstruct`, and uses a shared `search(...)` helper that does prefix-first, then contains-based ranking.

## Events

None.

## Dependencies

This feature depends on `user/` for user discovery and on `friendship/` for `isFriend` / `requestPending` enrichment. Friend-request UIs and friend-discovery flows depend on the `/api/v1/users/search` endpoint, while onboarding/profile forms can depend on the in-memory `/api/v1/search/*` endpoints.

## Key Files

| File | Role |
|---|---|
| `search/adapter/in/rest/UserSearchController.java` | `/api/v1/users/search` endpoint |
| `search/application/usecase/UserSearchUseCaseService.java` | User discovery + friendship/request annotations |
| `user/adapter/out/persistence/UserJpaRepository.java` | Case-insensitive `username/fullName` search query |
| `search/adapter/in/rest/SearchController.java` | `/api/v1/search/*` autocomplete endpoints |
| `search/application/usecase/SearchUseCaseService.java` | In-memory datasets and prefix/contains matching |
| `search/adapter/in/rest/dto/UserSearchResponse.java` | User-search payload |

## Known Limitations

- The role/company/university/language datasets are hard-coded in `search/application/usecase/SearchUseCaseService.java`; refreshing them requires a code change and redeploy.
- The general `/api/v1/search/*` endpoints still require authentication because `security/SecurityConfig.java` ends with `anyRequest().authenticated()` and does not whitelist them.

## Future Work

- `product-backlog.md` Epic 5.4 scopes "Trending users" as the next discovery feature; current search is text-only.
