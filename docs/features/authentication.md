# Authentication

## Status

Implemented

## Purpose

Lets a person create an Onnyth account, sign in, keep a session alive, and sign out. All identity and
credential storage is delegated to **Supabase Auth** — this server never stores passwords, and issues
no tokens of its own. On a user's first successful login, the server creates the corresponding local
`User` aggregate (the row every other feature joins against).

## User Capabilities

- Sign up with email + password.
- Log in and receive an access token + refresh token.
- Refresh an expiring access token without re-entering credentials.
- Log out (invalidate the current Supabase session).

## Business Rules

- Signup only registers the identity in Supabase — it does **not** create the local `User` row. The
  local `User` is created lazily on **first login**, with `emailVerified=true` and
  `profileComplete=false` (see `docs/features/profiles.md` for completion rules).
- All Supabase API calls are server-to-server: the server calls Supabase's `auth/v1/*` REST API with
  the configured `apikey` header (`supabase.anon-key`), never exposing Supabase credentials to clients.
- No endpoint under `/api/v1/auth/**` requires a JWT (see `SecurityConfig`); every other endpoint in the
  application defaults to `anyRequest().authenticated()`.

## API

**Base**: `/api/v1/auth` — public (no JWT required).

| Method | Path | Auth | Request | Response | Status |
|---|---|---|---|---|---|
| `POST` | `/signup` | Public | `AuthRequest { email, password }` | `SignupResponse { message, email }` | 201 |
| `POST` | `/login` | Public | `AuthRequest { email, password }` | `LoginResponse { accessToken, refreshToken, expiresAt, user }` | 200 |
| `POST` | `/refresh` | Public | `RefreshTokenRequest { refreshToken }` | `RefreshTokenResponse { accessToken, refreshToken, expiresAt }` | 200 |
| `POST` | `/logout` | Public path, but requires `Authorization` header | — | 204 No Content | 204 |

Error mapping: `EmailAlreadyExistsException` → 409, `InvalidSignupRequestException` → 400,
`InvalidSigninRequestException` → 401, `InvalidRefreshTokenException` → 401,
`SupabaseUnavailableException` → 503, `LogoutFailedException` → 500 (all via
`shared.exception.GlobalExceptionHandler`, see `docs/api/error-handling.md`).

## Data Model

No local table is owned by `auth/` itself — it reads/writes the `users` table (`user/` feature's
`UserEntity`) only on first login. See `docs/data/database-schema.md`.

## Domain Logic

```
Signup:  Client → AuthController → SupabaseAuthUseCaseService.signUp()
                                       → POST {supabaseUrl}/auth/v1/signup
                                       → SignupResponse (no local User row created)

Login:   Client → AuthController → SupabaseAuthUseCaseService.login()
                                       → POST {supabaseUrl}/auth/v1/token?grant_type=password
                                       → find-or-create local User (first login only)
                                       → LoginResponse { accessToken, refreshToken, expiresAt, user }

Refresh: Client → AuthController → SupabaseAuthUseCaseService.refresh()
                                       → POST {supabaseUrl}/auth/v1/token?grant_type=refresh_token
                                       → RefreshTokenResponse

Logout:  Client → AuthController → SupabaseAuthUseCaseService.logout()
                                       → POST {supabaseUrl}/auth/v1/logout (forwards Authorization header)
                                       → 204 No Content
```

Every Supabase call sets `Content-Type: application/json` and `apikey: {supabase.anon-key}`;
authenticated calls (refresh/logout) additionally forward the caller's `Authorization` header.
`RestTemplateConfig` provides the `RestTemplate` bean used for these calls with a 10s connect / 30s
read timeout.

Downstream of login/signup, every other authenticated controller extracts the user id from the JWT
subject claim:
```java
@AuthenticationPrincipal Jwt jwt
UUID userId = UUID.fromString(jwt.getSubject());
```
The JWT is validated as an OAuth2 Resource Server token against
`spring.security.oauth2.resourceserver.jwt.issuer-uri=${supabase.url}/auth/v1` — Spring Security
fetches Supabase's JWKS itself; this server never verifies the signature manually.

## Events

None.

## Dependencies

- **Supabase Auth** (external) — the actual identity provider; this feature is a thin server-side
  proxy plus local-user bootstrapping.
- **`user/`** — owns the `User` aggregate created on first login.
- **`security/SecurityConfig`** — defines which paths are public vs. JWT-protected for the whole app.

## Key Files

| File | Role |
|---|---|
| `auth/adapter/in/rest/AuthController.java` | REST controller, `/api/v1/auth/**` |
| `auth/application/usecase/SupabaseAuthUseCaseService.java` | Core use case — HTTP calls to Supabase Auth REST API, first-login `User` bootstrap |
| `auth/adapter/in/rest/dto/{AuthRequest,SignupResponse,LoginResponse,RefreshTokenRequest,RefreshTokenResponse}.java` | Public request/response records |
| `auth/adapter/out/client/dto/{SupabaseSignupResponse,SupabaseLoginResponse,SupabaseRefreshTokenResponse,SupabaseUser}.java` | Internal DTOs matching Supabase's own API shapes |
| `auth/application/exception/{InvalidSignupRequestException,InvalidSigninRequestException,InvalidRefreshTokenException,SupabaseUnavailableException}.java` | Feature-specific `ApiException` subclasses |
| `security/SecurityConfig.java` | Spring Security filter chain — public paths, JWT resource server config |
| `configuration/RestTemplateConfig.java` | `RestTemplate` bean (10s connect / 30s read timeout) used for all Supabase calls |

## Known Limitations

None currently known.

## Future Work

None currently scoped (no OAuth/social login or password reset story exists in
`docs/development/future-work.md` at time of writing).
