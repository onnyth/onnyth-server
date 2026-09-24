# Coding Standards

## Module layout

Every feature follows the per-feature hexagonal shape — see `docs/architecture/low-level-design.md`
and `docs/architecture/dependency-rules.md`. This document covers naming and style *within* that shape.

## Naming conventions

| Kind | Convention | Example |
|---|---|---|
| Use case service | `{Thing}UseCaseService` | `ProfileUseCaseService`, `BookmarkUseCaseService` |
| Repository port | `{Aggregate}Repository` | `UserRepository`, `BookmarkRepository` |
| Publisher port | `{Event}Publisher` | `BookmarkEventPublisher` |
| JPA entity | `{Aggregate}Entity` | `UserEntity`, `BookmarkEntity` |
| Spring Data repo | `{Aggregate}JpaRepository` | `UserJpaRepository` |
| Port implementation | `{Aggregate}RepositoryAdapter` | `UserRepositoryAdapter` |
| Persistence mapper | `{Aggregate}PersistenceMapper` | `BookmarkPersistenceMapper` |
| Exception | `{Condition}Exception`, extends `ApiException` | `UsernameAlreadyExistsException` |
| Request DTO | `{Thing}Request` | `ProfileUpdateRequest`, `CreateBookmarkRequest` |
| Response DTO | `{Thing}Response` | `ProfileCardResponse` |

## DTOs

- Java `record`s, living in `adapter/in/rest/dto/` next to the controller that owns them.
- Request DTOs carry Jakarta Validation annotations (`@NotNull`, `@Size`, `@Min`/`@Max`, `@ValidUri`, …).
- Response DTOs commonly use `@Builder` plus a static factory (`fromUser(...)`, `fromDomain(...)`,
  `fromPage(...)`) that maps from the domain model — construct via the factory, not field-by-field, so
  the mapping logic has one home.

## Domain models

- `domain/model` classes are framework-free: no JPA, no Spring.
- Most are Lombok `@Builder` + `@Getter`/`@Setter` + `@NoArgsConstructor`/`@AllArgsConstructor` — even
  where this makes them mutable — because the use-case layer follows a mutate-then-save pattern
  throughout the codebase. A handful (e.g. `Bookmark`) are immutable.
- Simple pure-logic methods belong on the domain model itself when they're a property of the concept
  (e.g. `RankTier.fromScore(long)`, `User.checkAndUpdateProfileCompletion()`), not smeared across the
  use case.

## Persistence

- JPA annotations belong only in `adapter/out/persistence/*Entity.java`.
- Lombok `@Builder`, `@Getter`, `@Setter`, `@NoArgsConstructor`, `@AllArgsConstructor` on entities.
- `UUID` primary keys throughout, typically `@GeneratedValue(strategy = GenerationType.AUTO)`.
- Spring Data method-name-derived queries are preferred over `@Query` where the method name stays
  readable; drop to `@Query`/JPQL for joins across tables (e.g. `FeedEventRepository.findFriendFeed`).

## Application layer

- Constructor injection via Lombok `@RequiredArgsConstructor` — no field injection (`@Autowired` on
  fields) anywhere in the codebase.
- `@Transactional` on every write-path use-case method; read-only paths that matter for performance use
  `@Transactional(readOnly = true)` (e.g. `ProfileUseCaseService.getProfileCard`).
- Repository/publisher dependencies are always behind an `application/port` interface — a use case
  never imports a class from `adapter/out/*`.
- Feature-specific `ApiException` subclasses for anything API-facing; never throw a raw
  `RuntimeException`/`IllegalStateException` across a use-case boundary that a controller will surface
  to a client.

## REST adapters

- `@RestController` classes always return `ResponseEntity<T>`.
- `@AuthenticationPrincipal Jwt jwt` → `UUID.fromString(jwt.getSubject())` to get the caller's id —
  never trust a client-supplied "current user" id.
- `@Operation`, `@ApiResponses`, `@Tag` (springdoc) on every endpoint — see `docs/api/openapi.md`.
- `/api/v1/` route prefix for new endpoints (see `docs/architecture/dependency-rules.md` #9 for the one
  legacy exception).

## Logging

Lombok `@Slf4j` on classes that log; `logging.level.com.onnyth=INFO` by default. No structured/JSON
logging is configured (see `docs/engineering/observability.md`).

## Style enforcement

There is no automated linter/formatter (no Checkstyle/Spotless/etc. configured in `pom.xml`) — these
conventions are enforced by code review and by following the shape of existing, similar code in the
feature you're touching. When in doubt, match the nearest existing example rather than inventing a new
pattern.
