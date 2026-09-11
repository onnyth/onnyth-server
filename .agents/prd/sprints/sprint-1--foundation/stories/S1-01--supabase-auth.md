# S1-01 — Supabase Auth Integration

> **Status**: `DONE`
> **Priority**: `HIGH`
> **Sprint**: Sprint 1

## User Story

> As a user, I want to sign up, log in, refresh my token, and log out so that I can securely access the app.

## Acceptance Criteria

- [x] Signup registers user with Supabase and returns confirmation-pending message
- [x] Login authenticates via Supabase, creates local User on first login, returns JWT tokens
- [x] Refresh endpoint exchanges refresh token for new access token
- [x] Logout invalidates session on Supabase
- [x] All non-auth endpoints require valid JWT
- [x] Public profile card endpoint accessible without JWT

## Delivered Components

| Component | File |
|---|---|
| Service | `SupabaseAuthService.java` |
| Controller | `AuthController.java` (4 endpoints) |
| Security | `SecurityConfig.java` (OAuth2 JWT) |
| Config | `RestTemplateConfig.java` |
| DTOs | `AuthRequest`, `LoginResponse`, `SignupResponse`, `RefreshTokenRequest/Response`, `SupabaseSession` |
| Supabase DTOs | `SupabaseSignupResponse`, `SupabaseLoginResponse`, `SupabaseRefreshTokenResponse`, `SupabaseUser` |
| Exceptions | `EmailAlreadyExistsException`, `InvalidSignupRequestException`, `InvalidSigninRequestException`, `InvalidRefreshTokenException`, `SupabaseUnavailableException`, `LogoutFailedException` |
| Error Handler | `GlobalExceptionHandler.java` |
| Base Exception | `ApiException.java`, `ApiErrorResponse.java` |

## Skills Updated

- `authentication/SKILL.md` — Created
- `error-handling/SKILL.md` — Created
- `api-reference/SKILL.md` — Created with auth endpoints
- `conventions/SKILL.md` — Established DTO, service, controller patterns
