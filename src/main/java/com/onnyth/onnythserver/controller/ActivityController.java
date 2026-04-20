package com.onnyth.onnythserver.controller;

import com.onnyth.onnythserver.dto.*;
import com.onnyth.onnythserver.models.StatDomain;
import com.onnyth.onnythserver.service.ActivityService;
import com.onnyth.onnythserver.service.ActivityTypeService;
import com.onnyth.onnythserver.service.LevelService;
import com.onnyth.onnythserver.service.StreakService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/activities")
@RequiredArgsConstructor
@Tag(name = "Activities", description = "Activity logging, types, history, and status")
public class ActivityController {

    private final ActivityService activityService;
    private final ActivityTypeService activityTypeService;
    private final LevelService levelService;
    private final StreakService streakService;

    @Operation(summary = "Get activity types", description = "List all active activity types, optionally filtered by category")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Activity types retrieved")
    })
    @GetMapping("/types")
    public ResponseEntity<List<ActivityTypeResponse>> getActivityTypes(
            @RequestParam(required = false) StatDomain category) {
        return ResponseEntity.ok(activityTypeService.getActivityTypes(category));
    }

    @Operation(summary = "Log an activity", description = "Log an activity completion, awards XP, checks level-up and streak")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Activity logged, XP awarded"),
            @ApiResponse(responseCode = "404", description = "Activity type not found"),
            @ApiResponse(responseCode = "429", description = "Activity is on cooldown")
    })
    @PostMapping("/log")
    public ResponseEntity<ActivityLogResponse> logActivity(
            @Valid @RequestBody LogActivityRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return ResponseEntity.ok(activityService.logActivity(userId, request.activityTypeId()));
    }

    @Operation(summary = "Get activity history", description = "Returns paginated activity history for the authenticated user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Activity history retrieved")
    })
    @GetMapping("/history")
    public ResponseEntity<Page<ActivityLogResponse>> getActivityHistory(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return ResponseEntity.ok(activityService.getActivityHistory(userId, PageRequest.of(page, size)));
    }

    @Operation(summary = "Get activity status", description = "Returns what the user logged today and current cooldowns")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Activity status retrieved")
    })
    @GetMapping("/status")
    public ResponseEntity<ActivityStatusResponse> getActivityStatus(@AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return ResponseEntity.ok(activityService.getActivityStatus(userId));
    }

    @Operation(summary = "Get level progress", description = "Returns current level, title, XP progress to next level")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Level progress retrieved")
    })
    @GetMapping("/level")
    public ResponseEntity<LevelProgressResponse> getLevelProgress(@AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return ResponseEntity.ok(levelService.getLevelProgress(userId));
    }

    @Operation(summary = "Get streak info", description = "Returns the user's current and longest streak")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Streak data retrieved")
    })
    @GetMapping("/streaks")
    public ResponseEntity<StreakResponse> getStreak(@AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return ResponseEntity.ok(streakService.getStreak(userId));
    }
}
