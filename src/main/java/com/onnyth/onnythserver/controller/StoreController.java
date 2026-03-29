package com.onnyth.onnythserver.controller;

import com.onnyth.onnythserver.dto.CosmeticItemResponse;
import com.onnyth.onnythserver.dto.EquipRequest;
import com.onnyth.onnythserver.dto.PurchaseRequest;
import com.onnyth.onnythserver.models.CosmeticCategory;
import com.onnyth.onnythserver.service.CosmeticService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/store")
@RequiredArgsConstructor
@Tag(name = "Store", description = "Cosmetic store — browse, purchase, and equip items")
public class StoreController {

    private final CosmeticService cosmeticService;

    @Operation(summary = "Get store items", description = "List all active cosmetic items, optionally filtered by category")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Store items retrieved")
    })
    @GetMapping("/items")
    public ResponseEntity<List<CosmeticItemResponse>> getStoreItems(
            @RequestParam(required = false) CosmeticCategory category,
            @AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return ResponseEntity.ok(cosmeticService.getStoreItems(userId, category));
    }

    @Operation(summary = "Purchase item", description = "Purchase a cosmetic item using XP")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Item purchased"),
            @ApiResponse(responseCode = "400", description = "Insufficient XP"),
            @ApiResponse(responseCode = "404", description = "Item not found"),
            @ApiResponse(responseCode = "409", description = "Item already owned")
    })
    @PostMapping("/purchase")
    public ResponseEntity<CosmeticItemResponse> purchaseItem(
            @Valid @RequestBody PurchaseRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return ResponseEntity.ok(cosmeticService.purchaseItem(userId, request.itemId()));
    }

    @Operation(summary = "Equip item", description = "Equip an owned cosmetic item")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Item equipped, returns all equipped items"),
            @ApiResponse(responseCode = "404", description = "Item not owned")
    })
    @PutMapping("/equip")
    public ResponseEntity<List<CosmeticItemResponse>> equipItem(
            @Valid @RequestBody EquipRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return ResponseEntity.ok(cosmeticService.equipItem(userId, request.itemId()));
    }

    @Operation(summary = "Get inventory", description = "Get all cosmetics owned by the user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Inventory retrieved")
    })
    @GetMapping("/inventory")
    public ResponseEntity<List<CosmeticItemResponse>> getInventory(@AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return ResponseEntity.ok(cosmeticService.getInventory(userId));
    }
}
