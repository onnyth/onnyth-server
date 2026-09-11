# S7-08 — Cosmetic Store: CosmeticService + Store Endpoints

> **Status**: `NOT_STARTED`
> **Priority**: 🟡 Should
> **Blocks**: S-047, S-049

## Goal

Build the cosmetic store system: list items, purchase with XP, equip/unequip, view inventory.

## Tasks

### CosmeticService
- [ ] Create `CosmeticService` with:
  - `getStoreItems(CosmeticCategory category)` — list active items, optionally filtered by category
  - `purchaseItem(UUID userId, UUID itemId)` — validate user has enough XP, deduct XP, create UserCosmetic
  - `equipItem(UUID userId, UUID itemId)` — validate ownership, set isEquipped = true
  - `getInventory(UUID userId)` — list user's owned cosmetics

### Exceptions
- [ ] `CosmeticNotFoundException` extends `ApiException` (404)
- [ ] `InsufficientXpException` extends `ApiException` (400)
- [ ] `CosmeticAlreadyOwnedException` extends `ApiException` (409)

### Controller Endpoints
- [ ] Create `StoreController` at `/api/v1/store`
- [ ] `GET /api/v1/store/items` — query: `category?`, returns `List<CosmeticItemResponse>`
- [ ] `POST /api/v1/store/purchase` — body: `PurchaseRequest`, returns purchase result
- [ ] `PUT /api/v1/store/equip` — body: `EquipRequest`, returns equipped items list
- [ ] `GET /api/v1/store/inventory` — returns `List<CosmeticItemResponse>` owned by user

### Seed Data
- [ ] `V21__seed_cosmetic_items.sql` — seed initial cosmetic items across categories

## Related Skills
- `conventions` — controller, service, exception patterns
- `error-handling` — custom exception hierarchy
