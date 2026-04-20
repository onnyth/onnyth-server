package com.onnyth.onnythserver.controller;

import com.onnyth.onnythserver.dto.*;
import com.onnyth.onnythserver.models.AchievementCategory;
import com.onnyth.onnythserver.service.AchievementService;
import io.swagger.v3.oas.annotations.Operation;
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
@RequestMapping("/api/v1/achievements")
@RequiredArgsConstructor
@Tag(name = "Achievements", description = "Achievement catalog, stats, badge display, and friend achievements")
public class AchievementController {

    private final AchievementService achievementService;

    // ─── Catalog ──────────────────────────────────────────────────────────────

    @Operation(summary = "Get all achievements", description = "Returns all active achievements with user's unlock status and progress")
    @GetMapping
    public ResponseEntity<List<AchievementResponse>> getAllAchievements(@AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return ResponseEntity.ok(achievementService.getAllAchievements(userId));
    }

    @Operation(summary = "Get achievements by category")
    @GetMapping("/category/{category}")
    public ResponseEntity<List<AchievementResponse>> getByCategory(
            @PathVariable AchievementCategory category,
            @AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return ResponseEntity.ok(achievementService.getAchievementsByCategory(userId, category));
    }

    @Operation(summary = "Get unlocked achievements", description = "Returns only unlocked achievements, sorted by unlock date DESC")
    @GetMapping("/unlocked")
    public ResponseEntity<List<AchievementResponse>> getUnlocked(@AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return ResponseEntity.ok(achievementService.getUnlockedAchievements(userId));
    }

    // ─── Stats ────────────────────────────────────────────────────────────────

    @Operation(summary = "Get achievement stats", description = "Returns total/unlocked counts and earned/total points")
    @GetMapping("/stats")
    public ResponseEntity<AchievementStatsResponse> getStats(@AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return ResponseEntity.ok(achievementService.getAchievementStats(userId));
    }

    // ─── Badge Display ────────────────────────────────────────────────────────

    @Operation(summary = "Get displayed badges", description = "Returns user's displayed badge slots (up to 3)")
    @GetMapping("/displayed")
    public ResponseEntity<List<DisplayedBadgeResponse>> getDisplayedBadges(@AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return ResponseEntity.ok(achievementService.getDisplayedBadges(userId));
    }

    @Operation(summary = "Update displayed badges", description = "Set up to 3 unlocked achievements as displayed badges")
    @PutMapping("/displayed")
    public ResponseEntity<List<DisplayedBadgeResponse>> updateDisplayedBadges(
            @Valid @RequestBody DisplayedBadgeRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return ResponseEntity.ok(achievementService.updateDisplayedBadges(userId, request.achievementIds()));
    }

    // ─── Friend Achievements ──────────────────────────────────────────────────

    @Operation(summary = "Get friend's achievements", description = "View a friend's unlocked achievements")
    @GetMapping("/user/{friendId}")
    public ResponseEntity<List<AchievementResponse>> getFriendAchievements(
            @PathVariable UUID friendId,
            @AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return ResponseEntity.ok(achievementService.getFriendAchievements(userId, friendId));
    }

    @Operation(summary = "Get friend's achievement stats")
    @GetMapping("/user/{friendId}/stats")
    public ResponseEntity<AchievementStatsResponse> getFriendStats(
            @PathVariable UUID friendId,
            @AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return ResponseEntity.ok(achievementService.getFriendAchievementStats(userId, friendId));
    }
}
