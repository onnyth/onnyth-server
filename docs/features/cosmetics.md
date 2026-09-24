# Cosmetics

## Status

Partially Implemented
Store browse/purchase/equip endpoints exist, but the seeded catalog and currency wiring are inconsistent with the current domain model.

## Purpose

This feature provides a cosmetic-store backend: browse active items, buy them, equip them, and inspect owned inventory. In the current codebase it is also the only place that mutates a user's active frame/background cosmetic references for profile presentation.

## User Capabilities

- Browse active store items, optionally by category, with ownership/equipped flags.
- Purchase a cosmetic item by id.
- Equip an owned item.
- View all owned cosmetics.

## Business Rules

- Purchases require the user to exist, the item to exist, the user not to already own it, and `user.onnythCoins >= item.price`.
- Purchasing deducts `item.price` from `users.onnyth_coins`, not from `users.xp`.
- Ownership is unique per `(user_id, cosmetic_item_id)`.
- New purchases start with `isEquipped = false`.
- Equipping requires ownership (`findByUserIdAndCosmeticItemId(...)`); missing ownership throws `CosmeticNotFoundException`.
- Equipping an item always sets that `user_cosmetics.is_equipped` flag to `true`.
- Equipping a `FRAME` also writes `users.active_frame_cosmetic_id`.
- Equipping a `BACKGROUND` also writes `users.active_background_cosmetic_id` and clears `users.active_background_color`.
- `getStoreItems()` and `getInventory()` both return `CosmeticItemResponse` enriched with `isOwned` and `isEquipped`.

## API

| Method | Path | Auth | Request | Response | Status |
|---|---|---|---|---|---|
| `GET` | `/api/v1/store/items` | JWT required | Optional query `category` (`CosmeticCategory`) | `List<CosmeticItemResponse>` | `200` |
| `POST` | `/api/v1/store/purchase` | JWT required | `PurchaseRequest` (`itemId`) | `CosmeticItemResponse` | `200 / 400 / 404 / 409` |
| `PUT` | `/api/v1/store/equip` | JWT required | `EquipRequest` (`itemId`) | `List<CosmeticItemResponse>` | `200 / 404` |
| `GET` | `/api/v1/store/inventory` | JWT required | — | `List<CosmeticItemResponse>` | `200` |

## Data Model

| Table | Entity / owner | Key columns | Constraints / source |
|---|---|---|---|
| `cosmetic_items` | `store/adapter/out/persistence/CosmeticItemEntity.java` | `id`, `name`, `description`, `preview_url`, `category`, `price`, `rarity`, `is_active`, `created_at` | Indexes on `category` and `is_active`; created by `src/main/resources/db/migration/V19__create_cosmetic_tables.sql`; default frame/background rows seeded by `V24__seed_frame_and_background_cosmetics.sql` |
| `user_cosmetics` | `store/adapter/out/persistence/UserCosmeticEntity.java` | `id`, `user_id`, `cosmetic_item_id`, `purchased_at`, `is_equipped` | Unique `(user_id, cosmetic_item_id)` plus `user_id` index; created by `src/main/resources/db/migration/V19__create_cosmetic_tables.sql` |
| `users` | `user/adapter/out/persistence/UserEntity.java` | `onnyth_coins`, `active_background_color`, `active_frame_cosmetic_id`, `active_background_cosmetic_id` | Active cosmetic refs are added by `src/main/resources/db/migration/V22__add_profile_cosmetic_and_ranking_fields.sql`; `onnyth_coins` is mapped in `UserEntity` but no matching Flyway migration exists under `src/main/resources/db/migration/` |

## Domain Logic

`store/application/usecase/StoreUseCaseService.java` is the full feature service. `getStoreItems()` loads the active catalog, the user's owned item ids, and the user's equipped item ids, then merges them into `CosmeticItemResponse`. `purchaseItem()` performs existence/ownership/balance checks, decrements `users.onnyth_coins`, inserts a `UserCosmetic`, and returns the purchased item as owned. `equipItem()` flips `isEquipped` on the owned row, updates the corresponding user-level active frame/background reference when relevant, and returns the refreshed inventory.

## Events

None.

## Dependencies

This feature depends on `user/` for the owning aggregate and active cosmetic references. Profile-card/profile-view features consume those user-level active cosmetic fields, while scoring/wealth reads `onnythCoins` as an input outside this module.

## Key Files

| File | Role |
|---|---|
| `store/adapter/in/rest/StoreController.java` | `/api/v1/store/**` REST API |
| `store/application/usecase/StoreUseCaseService.java` | Browse/purchase/equip/inventory logic |
| `store/application/port/{CosmeticItemRepository,UserCosmeticRepository}.java` | Hexagonal persistence ports |
| `store/adapter/out/persistence/{CosmeticItemEntity,UserCosmeticEntity}.java` | Table mappings |
| `store/adapter/out/persistence/{CosmeticItemJpaRepository,UserCosmeticJpaRepository}.java` | JPA queries |
| `store/domain/model/{CosmeticItem,UserCosmetic,CosmeticCategory,CosmeticRarity}.java` | Core domain models |
| `store/adapter/in/rest/dto/{CosmeticItemResponse,PurchaseRequest,EquipRequest}.java` | REST payloads |

## Known Limitations

- `StoreController`'s original design intent (per commit history) was for purchases to use XP, but `store/application/usecase/StoreUseCaseService.java` spends `users.onnyth_coins`.
- No code in `src/main/java/com/onnyth/onnythserver/` awards `onnythCoins`, and no Flyway migration under `src/main/resources/db/migration/` creates `users.onnyth_coins`; only the Supabase schema snapshot includes it.
- `src/main/resources/db/migration/V24__seed_frame_and_background_cosmetics.sql` seeds rarity `FREE`, but `store/domain/model/CosmeticRarity.java` only defines `COMMON`, `RARE`, `EPIC`, and `LEGENDARY`.
- Only free `BACKGROUND` and `FRAME` items are Flyway-seeded. The other `CosmeticCategory` enum values (`PROFILE_THEME`, `TITLE`, `BADGE_FRAME`, `AVATAR_SKIN`, `GLOW_EFFECT`, `BANNER`) have no seeded catalog entries or special equip handling.
- `equipItem()` never unequips previously equipped items, so multiple `user_cosmetics` rows can remain `is_equipped = true` at the same time.

## Future Work

- A broader multi-category seeded catalog was originally planned; the current Flyway history only seeds free frames/backgrounds.
- The store still needs a defined earning/spending loop for its chosen currency and, if desired, an explicit unequip / single-active-per-category flow.
