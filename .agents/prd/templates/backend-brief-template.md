# Backend Sprint Brief Template

> Produced alongside the client Agile Delivery Plan. Consumed by a backend agent/team to generate detailed server-side sprints.

## Backend Brief for Sprint NNN

> **Source PRD**: PRD-NNN
> **Client Sprint**: sprint-NNN
> **Date**: YYYY-MM-DD

---

### 1. API Endpoints Required

| Client Story | Method | Endpoint | Request Body | Response Body | Notes |
|---|---|---|---|---|---|
| S-XXX | GET | `/api/v1/xxx` | — | `{ field: type }` | _Description_ |
| S-XXX | POST | `/api/v1/xxx` | `{ field: type }` | `{ field: type }` | _Description_ |

---

### 2. Data Model Changes

| Entity | Change Type | Details |
|---|---|---|
| _EntityName_ | NEW | _New entity with fields: field1 (type), field2 (type)_ |
| _EntityName_ | MODIFY | _Add column: fieldX (type), default: value_ |
| _EnumName_ | NEW | _Values: VAL_A, VAL_B, VAL_C_ |

---

### 3. Business Logic / Services

| Service | Purpose | Triggered By |
|---|---|---|
| _ServiceName_ | _What it calculates/validates/processes_ | _Client story S-XXX_ |

---

### 4. Client → Backend Story Mapping

| Client Story | Backend Work Required | Backend Priority |
|---|---|---|
| S-XXX | _Endpoint + data model + service_ | 🔴 Must (blocks client) |
| S-XXX | _Endpoint only_ | 🟡 Should (client can use mock) |
| S-XXX | _None (pure frontend)_ | — |

---

### 5. Suggested Backend Sprint Ordering

| Order | Backend Task | Blocks Client Story | Notes |
|---|---|---|---|
| 1 | _Task description_ | S-XXX | _Must be done before client sprint_ |
| 2 | _Task description_ | S-XXX | _Can be done in parallel_ |

---

### 6. Notes for Backend Agent

_Any high-level context, existing patterns to follow, or constraints._
