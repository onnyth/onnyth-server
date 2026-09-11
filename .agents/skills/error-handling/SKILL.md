---
name: error-handling
description: Custom exception hierarchy, GlobalExceptionHandler, and standardized API error responses
---

# Error Handling

All exceptions are handled through a centralized `GlobalExceptionHandler` and follow a consistent `ApiErrorResponse` format.

> [!NOTE]
> `ApiException`, `ApiErrorResponse`, and `GlobalExceptionHandler` live in
> `com.onnyth.onnythserver.shared.exception` (the shared kernel), not a top-level `exceptions/`
> package. Feature-specific exceptions live in each feature's own `application/exception` package
> (e.g. `bookmark/application/exception/`, `user/application/exception/`,
> `lifestats/application/exception/`), not a shared `exceptions/` package.

## Exception Hierarchy

```
RuntimeException
└── ApiException (abstract, shared.exception.ApiException)   ← Base class with getHttpStatus()
    ├── EmailAlreadyExistsException            → 409 CONFLICT       (user/application/exception)
    ├── UsernameAlreadyExistsException          → 409 CONFLICT       (user/application/exception)
    ├── UserNotFoundException                  → 404 NOT_FOUND       (user/application/exception)
    ├── StatNotFoundException                  → 404 NOT_FOUND       (lifestats/application/exception)
    ├── InvalidStatValueException              → 400 BAD_REQUEST     (lifestats/application/exception)
    ├── QuestNotFoundException                 → 404 NOT_FOUND       (quest/application/exception)
    ├── QuestAlreadyCompletedException         → 409 CONFLICT        (quest/application/exception)
    ├── QuestExpiredException                  → 400 BAD_REQUEST     (quest/application/exception)
    ├── InvalidSignupRequestException          → 400 BAD_REQUEST     (auth/application/exception)
    ├── InvalidSigninRequestException          → 401 UNAUTHORIZED    (auth/application/exception)
    ├── InvalidRefreshTokenException           → 401 UNAUTHORIZED    (auth/application/exception)
    ├── SupabaseUnavailableException           → 503 SERVICE_UNAVAILABLE (auth/application/exception)
    ├── LogoutFailedException                  → 500 INTERNAL_SERVER_ERROR (shared/exception)
    ├── FileUploadException                    → 400 BAD_REQUEST     (profile/application/exception)
    ├── BookmarkNotFoundException               → 404 NOT_FOUND       (bookmark/application/exception)
    ├── MissingIdempotencyKeyException          → 400 BAD_REQUEST     (bookmark/application/exception)
    └── IdempotencyConflictException            → 409 CONFLICT        (bookmark/application/exception)
```
Each feature's exception package may have additional exceptions not exhaustively listed here — check
`{feature}/application/exception/` and the feature's own skill (if one exists) for the full list.

## ApiException Base Class

```java
// shared/exception/ApiException.java
public abstract class ApiException extends RuntimeException {
    protected ApiException(String message) {
        super(message);
    }
    public abstract HttpStatus getHttpStatus();
}
```

Every custom exception overrides `getHttpStatus()` to return its HTTP status code.

## API Error Response Format

```java
public record ApiErrorResponse(
    int statusCode,
    String error,          // e.g. "Not Found", "Conflict"
    String message,        // Human-readable description
    String path,           // Request URI
    Instant timestamp
) {}
```

Example response:
```json
{
    "statusCode": 404,
    "error": "Not Found",
    "message": "User not found: abc-123",
    "path": "/api/v1/profile",
    "timestamp": "2026-03-02T17:00:00Z"
}
```

## GlobalExceptionHandler

Located at `shared/exception/GlobalExceptionHandler.java`:

| Handler | Catches | Status |
|---|---|---|
| `handleApiException` | All `ApiException` subclasses | Varies per exception |
| `handleValidationException` | `MethodArgumentNotValidException` | 400 — joins field errors |
| `handleConstraintViolationException` | `ConstraintViolationException` (e.g. `@Validated` query params) | 400 — joins violation messages |
| `handleMethodArgumentTypeMismatchException` | `MethodArgumentTypeMismatchException` | 400 — "`{param}` has invalid format" |
| `handleMaxUploadSizeExceededException` | `MaxUploadSizeExceededException` | 413 — "File Too Large" |
| `handleUnexpectedException` | `Exception` (catch-all) | 500 — "Something went wrong" |

### Feature-scoped exception handlers

A feature can add its own `@RestControllerAdvice(assignableTypes = FooController.class)` alongside the
global one if it needs handling beyond what `handleApiException` provides generically — this does
**not** replace `GlobalExceptionHandler`, it supplements it for that controller only. Reference example:
`bookmark/adapter/in/rest/BookmarkExceptionHandler.java` (handles `BookmarkNotFoundException`,
`MissingIdempotencyKeyException`, `IdempotencyConflictException`). Prefer the generic
`ApiException`/`GlobalExceptionHandler` path for new exceptions unless there's a concrete reason a
feature-scoped handler is needed.

## Adding New Exceptions

1. Create a new class in `{feature}/application/exception/` extending `shared.exception.ApiException`
2. Override `getHttpStatus()` to return the appropriate HTTP status
3. Pass a descriptive message to `super(message)`
4. The `GlobalExceptionHandler` automatically handles it via `handleApiException()` — no extra
   wiring needed unless you also want a feature-scoped handler (see above)

Example:
```java
public class ResourceLockedException extends ApiException {
    public ResourceLockedException(String resource) {
        super("Resource is currently locked: " + resource);
    }

    @Override
    public HttpStatus getHttpStatus() {
        return HttpStatus.LOCKED; // 423
    }
}
```
