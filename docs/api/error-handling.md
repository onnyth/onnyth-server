# Error Handling

All exceptions are handled centrally by `shared.exception.GlobalExceptionHandler`
(`@RestControllerAdvice`) and returned in a single standardized shape.

## Response shape

```java
// shared/exception/ApiErrorResponse.java
public record ApiErrorResponse(
    int statusCode,
    String error,          // e.g. "Not Found", "Conflict"
    String message,        // human-readable description
    String path,            // request URI
    Instant timestamp
) {}
```

Example:
```json
{
    "statusCode": 404,
    "error": "Not Found",
    "message": "User not found: 3f2a...-...",
    "path": "/api/v1/profile",
    "timestamp": "2026-03-02T17:00:00Z"
}
```

## Exception hierarchy

```text
RuntimeException
└── ApiException (abstract, shared.exception.ApiException)   ← declares getHttpStatus()
    ├── {feature}.application.exception.*   ← one subclass per distinct API-facing error condition
    └── shared.exception.LogoutFailedException              → 500
```

Every feature owns its exceptions under its own `application/exception` package (e.g.
`bookmark.application.exception.BookmarkNotFoundException`, `user.application.exception.
UserNotFoundException`) — there is no shared top-level `exceptions/` package. Each feature's
`docs/features/*.md` documents its own exception → status-code mapping; this file documents the
*mechanism*, not an exhaustive cross-feature list.

## `GlobalExceptionHandler` handlers

| Handler | Catches | Status | Notes |
|---|---|---|---|
| `handleApiException` | Any `ApiException` subclass | `ex.getHttpStatus()` | The universal path for feature-thrown business errors |
| `handleValidationException` | `MethodArgumentNotValidException` | 400 | Joins all field errors into one message; `tags[]` is normalized to `tag` in the field name |
| `handleConstraintViolationException` | `ConstraintViolationException` | 400 | For `@Validated` query params (e.g. bookmark pagination bounds) |
| `handleMethodArgumentTypeMismatchException` | `MethodArgumentTypeMismatchException` | 400 | `"{param} has invalid format"` |
| `handleMaxUploadSizeExceededException` | `MaxUploadSizeExceededException` | 413 | File-too-large for multipart uploads (profile picture) |
| `handleUnexpectedException` | `Exception` (catch-all) | 500 | Generic `"Something went wrong"` — never leaks internal exception details to the client |

## `ApiException` base class

```java
public abstract class ApiException extends RuntimeException {
    protected ApiException(String message) { super(message); }
    public abstract HttpStatus getHttpStatus();
}
```

## Feature-scoped exception handlers

A feature may add its own `@RestControllerAdvice(assignableTypes = FooController.class)` **alongside**
the global handler if it needs behavior the generic `handleApiException` doesn't provide — this
supplements, it does not replace, `GlobalExceptionHandler`. The only current example is
`bookmark/adapter/in/rest/BookmarkExceptionHandler.java`. Prefer the generic path for new exceptions
unless there's a concrete reason a feature-scoped handler is needed.

## Adding a new exception

1. Create a class in `{feature}/application/exception/` extending `shared.exception.ApiException`.
2. Override `getHttpStatus()`.
3. Pass a descriptive message to `super(message)`.
4. No additional wiring is required — `GlobalExceptionHandler` picks it up automatically via
   `handleApiException`.

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
