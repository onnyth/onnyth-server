# API Overview

## Base URL and versioning

- Base path: `/api`
- Versioned feature endpoints: `/api/v1/**` (the convention for all new endpoints — see
  `docs/architecture/dependency-rules.md` #9)
- One legacy unprefixed contract remains: `/api/users/**` (admin CRUD, predates the `/v1` convention)
  and its one public sibling `/api/v1/users/{userId}/card`.

There is no API version negotiation beyond the URL prefix — there is only ever one live version of
each endpoint (no `/api/v2/` exists today). See `docs/api/versioning.md`... *(not yet needed — revisit
if/when a breaking change requires a second version.)*

## Authentication

Every endpoint requires a valid Supabase-issued JWT (`Authorization: Bearer <token>`) **except**:

| Path | Why public |
|---|---|
| `/api/v1/auth/**` | Signup/login/refresh/logout — can't require a token to get a token |
| `/api/v1/users/*/card` | Public profile card — viewable by anyone, including logged-out visitors |
| `/api/v1/health`, `/actuator/health/**` | Health checks (load balancer / Railway) |
| `/swagger-ui/**`, `/swagger-ui.html`, `/api-docs/**`, `/v3/api-docs/**` | API documentation UI |

Everything else is `anyRequest().authenticated()` (see `security/SecurityConfig.java`). There is no
role-based authorization anywhere in the system today — "authenticated" is the only tier. See
`docs/engineering/security.md` for the implications (e.g. `/api/users/**` admin CRUD has no admin-role
check).

## Discovering endpoints

The full, per-feature endpoint reference lives in each feature's `docs/features/*.md` "API" section
(the authoritative source, verified against controller source). For an interactive, always-current
view, use Swagger UI (`/swagger-ui.html`) / OpenAPI JSON (`/api-docs`) — see `docs/api/openapi.md`.

## Conventions

See `docs/api/conventions.md` for request/response shape, naming, and status-code conventions, and
`docs/api/error-handling.md` for the error response format.
