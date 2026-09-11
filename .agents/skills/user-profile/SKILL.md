---
name: user-profile
description: User profile management — CRUD, picture upload via Supabase Storage, profile card, and profile completion logic
---

# User Profile Management

Manages user profile data including username, full name, profile picture (via Supabase Storage), and the gamified profile card.

## Key Files

| File | Role |
|---|---|
| `service/ProfileService.java` | Profile business logic — get, update, picture upload, profile card |
| `service/StorageService.java` | Supabase Storage upload/delete for profile pictures |
| `controller/ProfileController.java` | REST controller at `/api/v1/profile/**` |
| `controller/UserController.java` | Admin CRUD at `/api/users/**` + public profile card at `/api/v1/users/{id}/card` |
| `models/User.java` | User entity with profile completion logic |

## Profile Completion Logic

The `User` entity has a `checkAndUpdateProfileCompletion()` method that sets `profileComplete = true` when all three required fields are filled:
```java
this.profileComplete = username != null && !username.isBlank()
        && fullName != null && !fullName.isBlank()
        && profilePic != null && !profilePic.isBlank();
```
This is called automatically after every profile update.

## Profile Operations

### Get Profile (`GET /api/v1/profile`)
- Returns `ProfileResponse` with all user fields including `totalScore`
- Requires JWT authentication

### Update Profile (`PUT /api/v1/profile`)
- Accepts `ProfileUpdateRequest` with optional fields: `username`, `fullName`, `profilePic`
- **Username validation**: 3-20 chars, alphanumeric + underscores, case-insensitive uniqueness check
- **Full name validation**: max 100 chars
- Only updates provided (non-null) fields
- Calls `checkAndUpdateProfileCompletion()` after update
- Throws `UsernameAlreadyExistsException` (409) if username taken

### Check Username (`GET /api/v1/profile/check-username/{username}`)
- Returns `{ "username": "...", "available": true/false }`
- Excludes current user's own username from the check

### Upload Profile Picture (`POST /api/v1/profile/picture`)
- Accepts `multipart/form-data` with `file` parameter
- **Allowed types**: JPEG, JPG, PNG, WebP
- **Max size**: 5MB
- Deletes old picture from Supabase Storage before uploading new one
- Stores file at: `profile-pics/{userId}/{randomUUID}.{ext}` in Supabase Storage
- Returns updated `ProfileResponse`

### Profile Card (`GET /api/v1/profile/card`)
- Returns `ProfileCardResponse` — gamified card with rank data
- Also available publicly at `GET /api/v1/users/{userId}/card`

### Rank Progress (`GET /api/v1/profile/rank`)
- Returns `RankProgressResponse` with progress towards next tier
- Delegates to `RankService.getRankProgress()`

## StorageService — Supabase Storage

- **Upload**: `POST {supabaseUrl}/storage/v1/object/{bucket}/{path}`
- **Delete**: `DELETE {supabaseUrl}/storage/v1/object/{bucket}/{path}`
- Uses `supabase.service-role-key` for authorization (falls back to `anon-key`)
- Validates file size (≤5MB) and content type before upload
- File path extraction for deletion parses the bucket name from the public URL

## DTOs

| DTO | Type | Fields |
|---|---|---|
| `ProfileUpdateRequest` | Request | `username?`, `fullName?`, `profilePic?` |
| `ProfileResponse` | Response | `id`, `email`, `username`, `fullName`, `profilePic`, `emailVerified`, `profileComplete`, `totalScore`, `createdAt`, `updatedAt` |
| `ProfileCardResponse` | Response | `userId`, `username`, `fullName`, `profilePic`, `totalScore`, `rankTier`, `rankBadgeUrl` |

## Adding Profile Features

When extending profile functionality:
1. Add fields to `User.java` entity (+ Flyway migration)
2. Update `ProfileUpdateRequest` if the field is user-editable
3. Update `ProfileResponse` and/or `ProfileCardResponse` factory methods
4. Add logic in `ProfileService`
5. Update `checkAndUpdateProfileCompletion()` if the new field is required for completion
