# Request Flow

## Anatomy of a typical authenticated write request

Example: `PUT /api/v1/profile` (update profile).

```text
1. Client sends PUT /api/v1/profile with Authorization: Bearer <supabase-jwt> and a JSON body.

2. Spring Security filter chain (security/SecurityConfig.java):
   - Path is not in the permitAll list, so the OAuth2 Resource Server filter runs.
   - JWT is validated against spring.security.oauth2.resourceserver.jwt.issuer-uri
     (${supabase.url}/auth/v1) — signature/issuer/expiry checked via Supabase's JWKS.
   - On success, an Authentication with the decoded Jwt is placed in the SecurityContext.
   - On failure: 401, request never reaches the controller.

3. Dispatch to ProfileController.updateProfile(...) (adapter/in/rest):
   - @AuthenticationPrincipal Jwt jwt is injected.
   - @Valid ProfileUpdateRequest body is bound + validated (Jakarta Validation);
     validation failure → MethodArgumentNotValidException, caught by GlobalExceptionHandler → 400.
   - UUID userId = UUID.fromString(jwt.getSubject()) extracts the caller's identity.
   - Controller calls exactly one use-case method: profileService.updateProfile(userId, request).

4. ProfileUseCaseService.updateProfile(...) (application/usecase), @Transactional:
   - Loads the User via UserRepository.findById(userId) (application/port) —
     throws UserNotFoundException (extends ApiException) if absent.
   - Applies business rules (username uniqueness check, field updates,
     checkAndUpdateProfileCompletion()).
   - Persists via UserRepository.save(user).
   - Returns a ProfileResponse (adapter/in/rest/dto), built via ProfileResponse.fromUser(user).

5. UserRepositoryAdapter (adapter/out/persistence) implements UserRepository:
   - Maps domain User ↔ UserEntity via UserPersistenceMapper.
   - Delegates to UserJpaRepository (Spring Data), which issues SQL against the users table
     (PostgreSQL, Supabase-hosted).

6. Controller wraps the returned ProfileResponse in ResponseEntity.ok(...) → 200 with JSON body.

7. If any step throws an ApiException subclass, GlobalExceptionHandler (shared.exception) intercepts
   it before it reaches the client, mapping it to a standard ApiErrorResponse with the exception's
   own getHttpStatus() (e.g. UsernameAlreadyExistsException → 409). See docs/api/error-handling.md.
```

## Anatomy of a write that also publishes an event

Example: `POST /api/v1/bookmarks` (the only feature that currently does this).

```text
1-3. Same as above (Security → Controller), plus: controller requires an Idempotency-Key header
     (missing → MissingIdempotencyKeyException → 400, via BookmarkController).

4. BookmarkUseCaseService.createBookmark(command, idempotencyKey), @Transactional:
   a. Compute canonical request hash ("url|title|sortedTags").
   b. safeGetIdempotencyRecord(key) — best-effort Redis read via IdempotencyService port.
        - Same key + same hash  → return the cached CreateBookmarkResponse immediately (no new
          bookmark row, no event — this is a replay, not a new write).
        - Same key + different hash → throw IdempotencyConflictException (409).
        - Miss (or Redis unavailable) → continue.
   c. Persist the new Bookmark via BookmarkRepository (port) → BookmarkRepositoryAdapter → JPA.
   d. Register TransactionSynchronizationManager.registerSynchronization(...).afterCommit(...) to
      publish BookmarkCreated via BookmarkEventPublisher (port) — deferred until the DB transaction
      actually commits, so a rollback never results in a published event.
   e. safeSaveIdempotencyRecord(key, hash, response) — best-effort Redis write, TTL 1 day.

5. After commit: KafkaBookmarkEventPublisher (adapter/out/kafka) publishes the JSON-serialized
   BookmarkCreated record to topic bookmark.created.v1, keyed by bookmarkId. A publish failure is
   caught and logged — it never fails or rolls back the already-committed HTTP response.

6. Any @KafkaListener consumer in its own consumer group (e.g. BookmarkCreatedConsumer, group
   onnyth-bookmark) picks up the event asynchronously, fully decoupled from the original request.
```

## Anatomy of an event-driven internal recalculation (no HTTP re-entry)

Example: a life-stat write triggering score + rank recalculation.

```text
1. A use case in registration/ or lifestats/ persists a new stat value, then publishes a
   shared.domain.event.StatChangedEvent(userId) via Spring's ApplicationEventPublisher
   (in-process, synchronous by default — not Kafka).

2. scoring/'s @EventListener @Transactional method receives the event, recomputes the user's
   weighted totalScore from the 5 StatDomain-weighted domain scores, and persists it on User.

3. scoring/ then calls into ranking/ to recompute and persist RankTier if it changed.

This is a same-JVM, same-request-thread Spring event, not a message broker hop — see
docs/features/scoring.md and docs/features/ranking.md for the exact mechanics.
```

## Public (unauthenticated) request example

`GET /api/v1/users/{userId}/card` and `GET /api/v1/auth/**` skip step 2's JWT validation entirely —
`SecurityConfig` explicitly `permitAll()`s these paths (plus `/api/v1/health`, `/actuator/health/**`,
and the Swagger UI/OpenAPI paths). Everything else defaults to `anyRequest().authenticated()`.
