# API Conventions

## Routing

- New endpoints: `/api/v1/{feature}/**`.
- Path segments are lowercase, kebab-case where multi-word (`/check-username/{username}`,
  `/background-color`).
- Resource-oriented nouns for collection endpoints (`/bookmarks`, `/quests`), verb-suffixed sub-actions
  where the operation isn't a plain CRUD verb (`/quests/{id}/complete`, `/account` for delete-self).

## HTTP methods and status codes

| Method | Use | Typical success status |
|---|---|---|
| `GET` | Read one or many resources | 200 |
| `POST` | Create a resource, or trigger a non-idempotent action (e.g. quest completion) | 201 (creation) or 200 (action) |
| `PUT` | Full update/replace of a resource | 200 |
| `DELETE` | Remove a resource | 204 |

`POST` that creates a resource returns 201 with a `Location` header when practical (see
`BookmarkController.create`). Actions that aren't resource creation (`POST /quests/{id}/complete`)
return 200 with a result body instead.

## Request DTOs

- Java `record`s in `adapter/in/rest/dto/` next to the controller that consumes them.
- Jakarta Validation annotations (`@NotNull`, `@Size`, `@Min`/`@Max`, custom constraints like
  `@ValidUri`) directly on the record components.
- Controllers annotate the parameter `@Valid` — validation failures are caught centrally (see
  `docs/api/error-handling.md`), never handled ad hoc in the controller body.

## Response DTOs

- Java `record`s, commonly with a `@Builder` and one or more static factory methods
  (`fromUser(User)`, `fromDomain(Bookmark)`, `fromPage(Page<T>)`) that map from the domain
  model/entity — controllers and use cases never hand-construct these ad hoc field-by-field outside
  the factory.
- Pagination: a dedicated `*PageResponse` wrapper per feature (e.g. `BookmarkPageResponse`) built via
  a `fromPage(Page<T>)` factory, rather than a single shared generic page wrapper across features.

## Authentication principal extraction

Every authenticated controller method extracts the caller's id the same way:
```java
@AuthenticationPrincipal Jwt jwt
UUID userId = UUID.fromString(jwt.getSubject());
```
Never trust a client-supplied user id for "the current user" — always derive it from the JWT subject.

## Pagination and filtering

Query parameters follow a consistent shape across paginated endpoints: `page` (0-indexed, default 0),
`size` (bounded, e.g. 1–100, default 20), plus feature-specific filters (`tag` for bookmarks,
`category` for leaderboard/achievements). Validated with `@Min`/`@Max` directly on the controller
method parameters (requires `@Validated` on the controller class, see `BookmarkController`).

## OpenAPI documentation

Controllers carry `@Tag` (class-level), `@Operation` (method-level summary/description), and
`@ApiResponses` (explicit per-status-code documentation) from `springdoc-openapi`. Every new endpoint
should include these three annotations — see `docs/api/openapi.md`.

## Full endpoint reference

There is no single hand-maintained master endpoint table in `docs/` — each feature's
`docs/features/*.md` "API" section is the authoritative, per-feature reference, cross-checked against
controller source. Use Swagger UI/`/api-docs` (`docs/api/openapi.md`) for a live, always-accurate view
across all features simultaneously.
