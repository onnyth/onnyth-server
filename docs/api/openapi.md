# OpenAPI / Swagger

## Where it lives

- **Swagger UI**: `/swagger-ui.html` (interactive, browsable) — enabled via
  `springdoc.swagger-ui.enabled=true`.
- **Raw OpenAPI JSON**: `/api-docs`.
- Both paths are `permitAll()` in `security/SecurityConfig.java` — reachable without a JWT.
- `springdoc.swagger-ui.operations-sorter=method`, `springdoc.swagger-ui.tags-sorter=alpha` — endpoints
  within a tag are sorted by HTTP method, tags are sorted alphabetically.

## Why this is the authoritative live API reference

This documentation set intentionally does **not** hand-maintain a single master endpoint table.
Endpoint tables inside `docs/features/*.md` are accurate as of their last verification against
controller source, but the generated OpenAPI spec (`/api-docs`) is **always** in sync with the running
code by construction — it is generated from the same `@Operation`/`@ApiResponses`/`@Tag` annotations
that exist on the controllers themselves, not maintained by hand.

Prefer `/api-docs` (or Swagger UI) over any hand-written doc when:
- You need to confirm the *exact current* request/response JSON shape for a client integration.
- A feature doc's endpoint table looks like it might be stale.

## Keeping annotations accurate

Every controller method should carry:
```java
@Operation(summary = "...", description = "...")
@ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "..."),
    @ApiResponse(responseCode = "404", description = "..."),
    // one entry per distinct status code the endpoint can return
})
```
plus a class-level `@Tag(name = "...", description = "...")`. When adding or changing an endpoint,
update these annotations in the same change — they are the source `/api-docs` generates from, so
letting them drift from actual behavior defeats the purpose of using generated docs at all.
