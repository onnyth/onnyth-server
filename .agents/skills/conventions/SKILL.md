---
name: conventions
description: Coding standards, design patterns, naming conventions, and best practices used throughout the Onnyth Server codebase
---

# Coding Conventions

This codebase now follows a **per-feature hexagonal structure** instead of the old top-level controller/service/repository/models split.

## Module Layout

Each bounded context should follow this shape when applicable:

```text
{feature}/
├── domain/model            # plain POJOs and enums
├── domain/event            # feature-owned domain events
├── application/port        # repository/outbound interfaces
├── application/usecase     # business logic (formerly services)
├── application/exception   # ApiException subclasses
├── adapter/in/rest         # controllers and REST DTOs
└── adapter/out/persistence # JPA entities, Spring Data repos, mappers, adapters
```

Current feature modules: `achievement`, `activity`, `auth`, `bookmark`, `feed`, `friendship`, `leaderboard`, `leveling`, `lifestats`, `profile`, `quest`, `ranking`, `registration`, `scoring`, `search`, `store`, `streak`, `user`, `xp`.

Cross-cutting infrastructure remains outside feature modules where appropriate, primarily `shared`, `configuration`, `security`, `system`, and `models` (placeholder social models only).

## Shared Kernel

Use `com.onnyth.onnythserver.shared` for cross-module contracts and utilities. It currently includes the `ApiException` base class plus shared domain contracts such as `shared.domain.model.StatDomain` and `shared.domain.event.StatChangedEvent`.

## DTO Pattern

- Request/response DTOs live next to the REST adapter that owns them: `adapter/in/rest/dto/`
- DTOs commonly use Java `record` types; response DTOs may expose static factory methods when mapping from domain objects
- Request DTOs use Jakarta Validation annotations when the endpoint accepts structured request bodies

## Domain and Persistence Split

- `domain/model` classes are framework-free POJOs/enums
- JPA annotations belong in `adapter/out/persistence/*Entity.java`
- Spring Data interfaces belong in `adapter/out/persistence/*JpaRepository.java`
- Port implementations belong in `adapter/out/persistence/*RepositoryAdapter.java`
- Mappers translate between domain models and persistence entities

## Application Layer

- Prefer constructor injection via Lombok `@RequiredArgsConstructor`
- Use `application/usecase/*UseCaseService` for business workflows
- Put repository dependencies behind `application/port` interfaces
- Mark write operations with `@Transactional`
- Throw feature-specific `ApiException` subclasses from `application/exception` for API-facing errors

## REST Adapter Conventions

- Controllers live in `adapter/in/rest/`
- Always return `ResponseEntity<T>`
- Extract authenticated user IDs from `@AuthenticationPrincipal Jwt jwt` via `UUID.fromString(jwt.getSubject())`
- Keep OpenAPI annotations on REST endpoints
- Use `/api/v1/` route prefixes unless an existing API contract already dictates otherwise
