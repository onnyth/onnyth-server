# Registration

## Status

Implemented

## Purpose

Owns the authenticated user's crash-recoverable onboarding flow. The feature stores step data in a
`registration_drafts` JSONB row, then atomically commits the collected data into `users` plus the
structured life-stat tables when the client calls the completion endpoint.

## User Capabilities

- Resume onboarding from the last saved step with full draft payload recovery.
- Save onboarding step payloads incrementally under `PHONE`, `NAME`, `IMAGE`, `OCCUPATION`, `WEALTH`,
  `PHYSIQUE`, `WISDOM`, and `CHARISMA`.
- Upload a registration profile picture before final commit.
- Finalize onboarding and materialize the draft into `users`, `user_occupation`, `user_wealth`,
  `user_physique`, `user_wisdom`, and `user_charisma`.

## Business Rules

- Step order is fixed by `RegistrationStep`: `PHONE` → `NAME` → `IMAGE` → `OCCUPATION` → `WEALTH` →
  `PHYSIQUE` → `WISDOM` → `CHARISMA`.
- Only `PHONE` and `NAME` are required for completion. Every later step is marked optional in
  `registration/domain/model/RegistrationStep.java`, so `POST /api/v1/registration/complete` succeeds as
  long as those two step keys exist in the draft.
- Draft-time validation is minimal:
  - `PHONE`: `phone` must be non-blank.
  - `NAME`: `username` and `displayName` must be non-blank; username uniqueness is checked with
    `UserRepository.existsByUsernameIgnoreCase(...)`, excluding the current user's existing username.
  - All other step payloads are accepted as arbitrary `Map<String,Object>` values.
- Drafts are keyed by user id, versioned with optimistic locking, and expire 30 days after creation.
- Commit-time normalization:
  - `username`, `displayName`, `profileType`, `jobTitle`, `companyName`, raw occupation fallbacks, and
    institution/company text are trimmed.
  - `incomeCurrency` is uppercased.
  - `languages` are lowercased.
  - `habitIds` and `educationLevel` are uppercased.
  - Invalid `fitnessLevel` strings are ignored with a warning instead of failing the commit.
- `OCCUPATION.isVerified` is inferred as `true` only when structured `jobTitle` and `companyName` are
  present and both raw fallback fields are blank, unless the draft explicitly supplies `isVerified`.

## API

Base path: `/api/v1/registration` — all endpoints require a JWT.

| Method | Path | Auth | Request | Response | Status |
|---|---|---|---|---|---|
| `GET` | `/status` | JWT | — | `RegistrationStatusResponse { currentStep, completedSteps, draftData, version }` | 200 |
| `PUT` | `/step/{stepKey}` | JWT | Raw JSON object (`Map<String,Object>`) for the named step | `RegistrationStepResponse { currentStep, completedSteps, version }` | 200 / 400 / 409 |
| `POST` | `/step/IMAGE/upload` | JWT | `multipart/form-data` with `file` | `RegistrationStepResponse` | 200 / 400 |
| `POST` | `/complete` | JWT | — | `RegistrationCompleteResponse { profileComplete, userId, username }` | 200 / 400 |

## Data Model

| Table | Entity class | Key columns / constraints | Migration(s) |
|---|---|---|---|
| `registration_drafts` | `registration/adapter/out/persistence/RegistrationDraftEntity.java` | `user_id` PK and FK to `users`; `current_step` CHECK over the 8 enum names; `draft_data` JSONB; `version`; `expires_at`; index on `expires_at` | `supabase/migrations/20260419_registration_drafts.sql` |
| `users` | `user/adapter/out/persistence/UserEntity.java` | Registration writes `phone`, `profile_type`, `username`, `full_name`, `profile_pic`, `profile_complete`, `updated_at` | `supabase/migrations/20260419_registration_drafts.sql`, `src/main/resources/db/migration/V1__add_profile_completion_fields.sql` |
| `user_occupation` | `lifestats/adapter/out/persistence/UserOccupationEntity.java` | One committed row is inserted with `is_current=true`; structured and raw fallback columns coexist | Base table in `supabase/migrations/20260412183914_remote_schema.sql`; structured columns added in `src/main/resources/db/migration/V21__add_structured_onboarding_fields.sql` |
| `user_wealth` | `lifestats/adapter/out/persistence/UserWealthEntity.java` | One row per user (`user_id` unique); registration fills bracket/currency/savings fields | Base table in `supabase/migrations/20260412183914_remote_schema.sql`; `income_currency` in `supabase/migrations/20260413_add_income_currency.sql`; `monthly_spending_bracket` in `src/main/resources/db/migration/V21__add_structured_onboarding_fields.sql` |
| `user_physique` | `lifestats/adapter/out/persistence/UserPhysiqueEntity.java` | One row per user (`user_id` unique); numeric height/weight/body-fat columns | `supabase/migrations/20260412183914_remote_schema.sql` |
| `user_wisdom` | `lifestats/adapter/out/persistence/UserWisdomEntity.java` | One row per user (`user_id` unique); `hobbies` JSONB stores `habitIds`; `languages` JSONB, `education_level`, `institution_name`, `graduation_year` added later | Base table in `supabase/migrations/20260412183914_remote_schema.sql`; structured additions in `src/main/resources/db/migration/V21__add_structured_onboarding_fields.sql` |
| `user_charisma` | `lifestats/adapter/out/persistence/UserCharismaEntity.java` | One row per user (`user_id` unique); onboarding adds `relationship_status` and `social_circle_size` | Base table in `supabase/migrations/20260412183914_remote_schema.sql`; extra columns in `supabase/migrations/20260419_registration_drafts.sql` |

## Domain Logic

```text
Client -> RegistrationController.saveStep(stepKey, payload)
       -> RegistrationUseCaseService.validateStep(...)
       -> create/find RegistrationDraft
       -> draft.mergeStepData(step, payload)
       -> advance currentStep to step.next()
       -> save draft row with incremented @Version

Client -> RegistrationController.completeRegistration()
       -> RegistrationCommitUseCaseService.commitRegistration(userId)
       -> require PHONE + NAME keys in draft
       -> mutate User fields (phone/name/image/profileType)
       -> insert structured lifestat rows for any optional steps that were supplied
       -> set user.profileComplete = true
       -> save User
       -> delete registration_drafts row
```

`GET /status` returns `RegistrationStatusResponse.empty()` when no draft exists: `currentStep=PHONE`, no
completed steps, empty `draftData`, and `version=0`.

## Events

None. `registration/` does not publish `StatChangedEvent` or any registration-complete event.

## Dependencies

- **`user/`** — owns the `User` row that onboarding mutates.
- **`lifestats/`** — owns the structured tables populated at commit time.
- **`profile/adapter/out/storage/StorageServiceAdapter`** — uploads the IMAGE step file and returns the
  stored URL.
- **Consumers**: `profile/` reads the committed user + lifestat data for profile cards; `scoring/` is
  expected to read the committed stat rows later.

## Key Files

| File | Role |
|---|---|
| `registration/adapter/in/rest/RegistrationController.java` | REST surface for status, step save, image upload, and completion |
| `registration/domain/model/RegistrationStep.java` | Ordered step enum, required/optional flags, `next()` logic |
| `registration/domain/model/RegistrationDraft.java` | In-memory draft aggregate backed by JSONB |
| `registration/application/usecase/RegistrationUseCaseService.java` | Draft lifecycle, per-step validation, status assembly |
| `registration/application/usecase/RegistrationCommitUseCaseService.java` | Atomic draft → normalized-table commit |
| `registration/adapter/out/persistence/{RegistrationDraftEntity,RegistrationDraftJpaRepository,RegistrationDraftRepositoryAdapter,RegistrationDraftPersistenceMapper}.java` | Persistence adapter for `registration_drafts` |
| `registration/adapter/in/rest/dto/{RegistrationStatusResponse,RegistrationStepResponse,RegistrationCompleteResponse}.java` | Public response DTOs |

## Known Limitations

- Completion only requires `PHONE` and `NAME`, yet `RegistrationCommitUseCaseService` unconditionally sets
  `user.profileComplete=true`; this bypasses `User.checkAndUpdateProfileCompletion()` and can mark a user
  complete without an image or optional stat data.
- `registration/adapter/in/rest/dto/*StepRequest.java` records are not used by the controller; the live API
  accepts raw maps instead. Some of those DTOs are also stale (`WisdomStepRequest` still uses
  `formalEducation` / `readingHabits`, which do not match commit keys like `educationLevel` / `habitIds`).
- `RegistrationDraftRepository.deleteExpiredDrafts(...)` exists, but no scheduler or startup cleanup calls
  it in current source, so expired drafts are not automatically purged.
- Registration completion does not trigger score recalculation or rank updates; newly committed stat rows
  keep their default `score=0` until some other code path calls `scoring/ScoreCalculationUseCaseService`.

## Future Work

None currently scoped beyond the onboarding redesign already reflected in
this code.
