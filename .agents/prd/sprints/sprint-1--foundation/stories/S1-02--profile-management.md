# S1-02 — Profile Management & Picture Upload

> **Status**: `DONE`
> **Priority**: `HIGH`
> **Sprint**: Sprint 1

## User Story

> As a user, I want to set up my profile (username, full name, picture) and see my profile card so that other users can identify me.

## Acceptance Criteria

- [x] Get own profile with all fields
- [x] Update profile with optional username, fullName, profilePic fields
- [x] Username uniqueness check (case-insensitive)
- [x] Upload profile picture via multipart form data (JPG, PNG, WebP, max 5MB)
- [x] Old profile picture deleted from Supabase Storage on new upload
- [x] Profile completion auto-calculated (username + fullName + profilePic all set)
- [x] Profile card endpoint with gamification data
- [x] Public profile card accessible without authentication

## Delivered Components

| Component | File |
|---|---|
| Service | `ProfileService.java`, `StorageService.java` |
| Controller | `ProfileController.java` (6 endpoints), `UserController.java` (admin CRUD + public card) |
| Entity | `User.java` (with `checkAndUpdateProfileCompletion()`) |
| Migration | `V1__add_profile_completion_fields.sql` |
| DTOs | `ProfileUpdateRequest`, `ProfileResponse`, `ProfileCardResponse` |
| Exceptions | `UsernameAlreadyExistsException`, `UserNotFoundException`, `FileUploadException` |
| Service | `UserService.java` (basic CRUD) |
| Repository | `UserRepository.java` |

## Skills Updated

- `user-profile/SKILL.md` — Created
- `data-layer/SKILL.md` — Created with User entity and V1 migration
- `api-reference/SKILL.md` — Added profile and user endpoints
