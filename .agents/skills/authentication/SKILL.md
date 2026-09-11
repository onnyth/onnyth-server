---
name: authentication
description: Supabase Auth integration — signup, login, refresh, logout flows and Spring Security JWT configuration
---

# Authentication

All authentication is delegated to **Supabase Auth**. The server acts as an OAuth2 Resource Server that validates Supabase-issued JWTs.

## Key Files

> [!NOTE]
> Paths below reflect the current per-feature hexagonal layout (see `.agents/skills/conventions/SKILL.md`
> and `docs/decisions/ADR-0001-hexagonal-architecture.md`). Earlier versions of this skill referenced a
> pre-migration `service/`/`controller/` layout that no longer exists.

| File | Role |
|---|---|
| `auth/application/usecase/SupabaseAuthUseCaseService.java` | Core auth use case — HTTP calls to Supabase Auth REST API |
| `auth/adapter/in/rest/AuthController.java` | REST controller at `/api/v1/auth/**` |
| `auth/adapter/out/client/dto/*` | Internal DTOs for Supabase's own request/response shapes (`SupabaseSignupResponse`, `SupabaseLoginResponse`, `SupabaseRefreshTokenResponse`, `SupabaseUser`) |
| `auth/application/exception/*` | `InvalidSignupRequestException`, `InvalidSigninRequestException`, `InvalidRefreshTokenException`, `SupabaseUnavailableException` |
| `security/SecurityConfig.java` | Spring Security filter chain with JWT validation |
| `configuration/RestTemplateConfig.java` | `RestTemplate` bean with 10s connect / 30s read timeouts |

## Auth Flows

### 1. Signup (`POST /api/v1/auth/signup`)
```
Client → AuthController → SupabaseAuthUseCaseService.signUp()
                              ↓
                         POST supabaseUrl/auth/v1/signup
                              ↓
                         Returns SignupResponse { message, email }
```
- Registers user in Supabase only — local `User` row is **NOT** created here
- Returns `201 Created` with confirmation-pending message
- Handles: `EmailAlreadyExistsException` (409), `InvalidSignupRequestException` (400)

### 2. Login (`POST /api/v1/auth/login`)
```
Client → AuthController → SupabaseAuthUseCaseService.login()
                              ↓
                         POST supabaseUrl/auth/v1/token?grant_type=password
                              ↓
                         Find or CREATE local User row (first login)
                              ↓
                         Returns LoginResponse { accessToken, refreshToken, expiresAt, user }
```
- **First login creates the local `User`** with `emailVerified=true`, `profileComplete=false`
- Returns `200 OK` with JWT tokens + user info
- Handles: `InvalidSigninRequestException` (401), `SupabaseUnavailableException` (503)

### 3. Token Refresh (`POST /api/v1/auth/refresh`)
```
Client → AuthController → SupabaseAuthUseCaseService.refresh()
                              ↓
                         POST supabaseUrl/auth/v1/token?grant_type=refresh_token
                              ↓
                         Returns RefreshTokenResponse { accessToken, refreshToken, expiresAt }
```
- Handles: `InvalidRefreshTokenException` (401)

### 4. Logout (`POST /api/v1/auth/logout`)
```
Client → AuthController → SupabaseAuthUseCaseService.logout()
                              ↓
                         POST supabaseUrl/auth/v1/logout (with Authorization header)
                              ↓
                         Returns 204 No Content
```
- Handles: `LogoutFailedException` (500)

## Security Configuration

```java
// SecurityConfig.java
http
    .csrf(disable)
    .authorizeHttpRequests(auth -> auth
        .requestMatchers("/api/v1/auth/**").permitAll()
        .requestMatchers("/api/v1/users/*/card").permitAll()
        .requestMatchers("/api/v1/health").permitAll()
        .requestMatchers("/actuator/health/**").permitAll()
        .requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/api-docs/**", "/v3/api-docs/**").permitAll()
        .anyRequest().authenticated())
    .oauth2ResourceServer(oauth2 -> oauth2.jwt(defaults));

// JWT issuer URI: ${supabase.url}/auth/v1
```

## How JWT Subjects Are Used

All authenticated controllers extract the user ID from the JWT `sub` claim:
```java
@AuthenticationPrincipal Jwt jwt
UUID userId = UUID.fromString(jwt.getSubject());
```

## Supabase HTTP Headers Pattern

Every call to the Supabase API uses:
```java
headers.setContentType(MediaType.APPLICATION_JSON);
headers.set("apikey", supabaseAnonKey);
// For authenticated calls:
headers.set(HttpHeaders.AUTHORIZATION, authorizationHeader);
```

## Request/Response DTOs

| DTO | Fields |
|---|---|
| `AuthRequest` | `email`, `password` |
| `SignupResponse` | `message`, `email` |
| `LoginResponse` | `accessToken`, `refreshToken`, `expiresAt`, `user` (nested `UserInfo`) |
| `LoginResponse.UserInfo` | `id`, `email`, `username`, `fullName`, `profilePic` |
| `RefreshTokenRequest` | `refreshToken` |
| `RefreshTokenResponse` | `accessToken`, `refreshToken`, `expiresAt` |

## Supabase DTOs (internal)

Located in `auth/adapter/out/client/dto/`:
- `SupabaseSignupResponse` — `id`, `email`
- `SupabaseLoginResponse` — `accessToken`, `refreshToken`, `expiresAt`, `supabaseUser`
- `SupabaseRefreshTokenResponse` — `accessToken`, `refreshToken`, `expiresAt`
- `SupabaseUser` — `id`, `email`

## Adding New Auth Features

When adding new auth features (e.g., OAuth social login, password reset):
1. Add the Supabase API call in `SupabaseAuthUseCaseService`
2. Add the endpoint in `AuthController`
3. Update `SecurityConfig` if the endpoint should be public
4. Create corresponding request/response DTOs as Java records
