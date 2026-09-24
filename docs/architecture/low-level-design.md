# Low-Level Design

This document describes how the architectural decisions in `docs/architecture/high-level-design.md`
are actually implemented at the package/class level — the shape every feature module should follow.

## Standard feature module layout

```text
{feature}/
├── domain/
│   ├── model/            # framework-free POJOs / enums — no JPA, no Spring
│   └── event/             # feature-owned domain events (only if the feature publishes events)
├── application/
│   ├── port/              # outbound interfaces: repository ports, publisher ports
│   │   └── out/           # (some features nest publisher-style ports under port/out)
│   ├── usecase/           # business logic ("...UseCaseService"), orchestrates ports
│   └── exception/         # feature-specific exceptions extending shared.exception.ApiException
├── adapter/
│   ├── in/
│   │   ├── rest/          # controllers + request/response DTOs (records), in rest/dto/
│   │   └── kafka/         # inbound Kafka consumers (only if the feature consumes events)
│   └── out/
│       ├── persistence/   # JPA entities, Spring Data repos, mappers, port implementations
│       ├── kafka/         # outbound Kafka publishers/topic constants (only if the feature publishes)
│       └── client|storage/# outbound HTTP clients (auth → Supabase Auth, profile → Supabase Storage)
```

Not every feature has every folder — `adapter/in/kafka` and `adapter/out/kafka` only exist where a
feature actually consumes/publishes events (currently only `bookmark`); `domain/event` only exists
where a feature has its own event type.

## Layer responsibilities

### `domain/model`
Plain Java classes/enums/records representing business concepts, with zero framework imports. May
contain small pure-logic methods (e.g. `RankTier.fromScore(long)`, `User.checkAndUpdateProfileCompletion()`).
Some domain models are mutable (`@Getter @Setter @Builder`) to match a widespread
mutate-then-save pattern in the use-case layer — this is a deliberate, documented pragmatic choice
(see `user/domain/model/User.java`'s Javadoc), not an oversight.

### `application/port`
Interfaces only. A repository port typically mirrors the read/write operations a use case needs
(`findById`, `save`, `existsByUsernameIgnoreCase`, …) — not a generic CRUD interface. A publisher port
(e.g. `BookmarkEventPublisher`) abstracts "publish this domain event" away from the concrete messaging
technology.

### `application/usecase`
One `@Service` class per cohesive business capability, named `{Thing}UseCaseService`. Constructor
injection via Lombok `@RequiredArgsConstructor`. Write operations are `@Transactional`. Throws
feature-specific `ApiException` subclasses for anything API-facing. This is where business rules,
validation beyond simple bean validation, and cross-port orchestration live.

### `application/exception`
Each exception extends `shared.exception.ApiException` and implements `getHttpStatus()`. No feature
needs its own exception *handler* unless it needs behavior beyond the generic one (see
`docs/api/error-handling.md`).

### `adapter/in/rest`
`@RestController` classes. Always return `ResponseEntity<T>`. Extract the authenticated user id via
`@AuthenticationPrincipal Jwt jwt` → `UUID.fromString(jwt.getSubject())`. Keep OpenAPI (`@Operation`,
`@ApiResponses`, `@Tag`) annotations here. DTOs live in a `dto/` sub-package next to the controller
that owns them, as Java `record`s with Jakarta Validation annotations (`@Valid`, `@NotNull`, `@Size`,
…) and static `fromDomain(...)`/`fromUser(...)` factory methods for mapping from domain objects.

### `adapter/out/persistence`
- `*Entity.java` — the JPA-annotated class, mapped 1:1 to a table.
- `*JpaRepository.java` — a `JpaRepository`/`Repository` interface (Spring Data derives queries from
  method names, or `@Query` for complex ones).
- `*PersistenceMapper.java` — translates domain model ↔ entity.
- `*RepositoryAdapter.java` — implements the `application/port` interface, delegates to the JPA
  repository + mapper. This is the *only* class allowed to know both the domain model and the entity.

### `adapter/out/kafka` / `adapter/in/kafka`
Topic name constants (versioned, e.g. `bookmark.created.v1`), a publisher implementing the outbound
port, and `@KafkaListener` consumer(s) with their own `groupId`.

## Cross-feature dependency shape

A feature that needs another feature's data depends on that feature's `application/port` interface
(and, transitively, its `domain/model`) — never on its `adapter/out/persistence` classes. Concretely:

```java
// profile/application/usecase/ProfileUseCaseService.java depends on:
com.onnyth.onnythserver.user.application.port.UserRepository
com.onnyth.onnythserver.lifestats.application.port.UserWealthRepository   // + 4 more domain repos
com.onnyth.onnythserver.streak.application.port.UserStreakRepository
```

This is legal and expected — hexagonal boundaries are *per-feature*, not *per-request*; a use case is
free to depend on many other features' ports to assemble a rich read-model (see
`docs/features/profiles.md`, `docs/features/feed.md` for the most heavily cross-cutting examples).

## Related documents

- `docs/architecture/dependency-rules.md` — the specific rules a new change must respect
- `docs/architecture/module-structure.md` — current package tree and feature inventory
- `docs/engineering/coding-standards.md` — naming and style conventions within this shape
