# S{N}-{XX} — {Story Title}

> **Status**: `PLANNED` | `IN_PROGRESS` | `DONE`
> **Priority**: 🔴 Must | 🟡 Should
> **Sprint**: Sprint {N}
> **Blocks Client Stories**: S-XXX, S-XXX

## What & Why

> Brief description of what this story delivers and which client stories it unblocks.

## API Endpoints (from Brief §1)

| Method | Endpoint | Request Body | Response Body | Notes |
|---|---|---|---|---|
| GET | `/api/v1/xxx` | — | `{ field: type }` | _Description_ |
| POST | `/api/v1/xxx` | `{ field: type }` | `{ field: type }` | _Description_ |

## Technical Tasks

### Data Model (from Brief §2)
- [ ] Create/modify entity: `{Entity}.java` — {fields}
- [ ] Create Flyway migration: `V{X}__{description}.sql`
- [ ] Create/modify enum: `{Enum}.java` — {values}

### Services (from Brief §3)
- [ ] Create/modify service: `{Service}.java` — {purpose}

### Controllers & DTOs
- [ ] Create request DTO: `{Request}.java`
- [ ] Create response DTO: `{Response}.java`
- [ ] Create/modify controller: `{Controller}.java` — endpoints above
- [ ] Add exception(s): `{Exception}.java`

### Tests
- [ ] Unit test: `{ServiceTest}.java`
- [ ] Controller test: `{ControllerTest}.java`
- [ ] Integration test (if applicable)

## Related Skills

> Skills the AI should read before starting this story.

- `conventions` — coding patterns
- `data-layer` — entity/migration patterns
- `api-reference` — existing endpoints (avoid conflicts)
- `{other relevant skill}`

## Skill Updates Required

> After completing this story, which skills need updating?

- [ ] `data-layer/SKILL.md` — {what changed}
- [ ] `api-reference/SKILL.md` — {new endpoints}
- [ ] `error-handling/SKILL.md` — {new exceptions}
- [ ] `testing/SKILL.md` — {new test files}

## Notes

_Any additional context from backend brief §6 or implementation decisions._
