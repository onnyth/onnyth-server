# Security

## Authentication

Every request is authenticated via a Supabase-issued JWT validated as an OAuth2 Resource Server token
(`spring-boot-starter-oauth2-resource-server`). There is no session state, no server-issued token, and
no password ever touches this server directly — see `docs/features/authentication.md`.

## Authorization

**There is no role-based authorization anywhere in this codebase.** `SecurityConfig` has exactly two
tiers: `permitAll()` (an explicit allowlist of public paths) and `anyRequest().authenticated()`
(everything else). Concretely, this means:

- `/api/users/**` (list all users, fetch any user by id/email, create/update/delete any user) requires
  only *a* valid JWT — any authenticated user can call it. See `docs/features/users.md` Known
  Limitations.
- `POST /api/v1/bookmarks` and friends are not scoped to the caller's own bookmarks — any authenticated
  user can read/update/delete any other user's bookmark. See `docs/features/bookmarks.md`.
- There is no concept of an "admin" role in the JWT claims or anywhere in `SecurityConfig`.

Any new endpoint that should be restricted to a subset of authenticated users needs its own
authorization check inside the use case (there is no framework-level role gate to lean on today).

## Public endpoints (no JWT required)

| Path | Reason |
|---|---|
| `/api/v1/auth/**` | Can't require a token to obtain one |
| `/api/v1/users/*/card` | Publicly viewable profile card |
| `/api/v1/health`, `/actuator/health/**` | Health checks |
| `/swagger-ui/**`, `/swagger-ui.html`, `/api-docs/**`, `/v3/api-docs/**` | API documentation |

## Secrets management

`SUPABASE_URL`, `SUPABASE_ANON_KEY`, `SUPABASE_SERVICE_ROLE_KEY`, `DATABASE_URL`,
`DATABASE_USERNAME`, `DATABASE_PASSWORD`, `RAILWAY_TOKEN` are all supplied via environment
variables — `.env` locally (via `spring-dotenv`), GitHub Actions secrets in CI/CD. `.env.example`
documents variable names without real values. **Never hardcode or print real secret values in code,
commits, or documentation.**

## Input validation

- Jakarta Validation (`@Valid`, `@NotNull`, `@Size`, `@Min`/`@Max`, custom `@ValidUri`) on every request
  DTO — see `docs/api/conventions.md`.
- File upload validation (profile picture): content-type allowlist (JPEG/JPG/PNG/WebP), 5MB max size,
  enforced in `ProfileUseCaseService`/`StorageServiceAdapter` before the file reaches Supabase Storage.
- SQL injection: mitigated structurally — every query is via Spring Data JPA (derived queries or JPQL
  with bind parameters), no string-concatenated native SQL anywhere in the codebase.

## Idempotency and replay protection

Only `bookmark`'s `POST` endpoint has replay protection (`Idempotency-Key` header, see
`docs/decisions/ADR-0002-redis-idempotency.md`). No other unsafe endpoint in the system guards against
duplicate submission from a client retry.

## Rate limiting

None. There is no rate limiter, request-throttling filter, or API gateway in front of this service
today.

## Known gaps (see `docs/development/known-issues.md` for full detail)

- No role-based authorization (`/api/users/**` admin surface, bookmark ownership).
- No rate limiting on any endpoint, including `auth/**` (no brute-force protection beyond whatever
  Supabase itself enforces).
