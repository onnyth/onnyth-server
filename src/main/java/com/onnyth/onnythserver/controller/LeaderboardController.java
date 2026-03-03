package com.onnyth.onnythserver.controller;

import com.onnyth.onnythserver.dto.LeaderboardResponse;
import com.onnyth.onnythserver.service.LeaderboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * Controller for leaderboard endpoints.
 */
@RestController
@RequestMapping("/api/v1/leaderboards")
@RequiredArgsConstructor
@Tag(name = "Leaderboard", description = "Global leaderboard rankings by total score")
public class LeaderboardController {

    private final LeaderboardService leaderboardService;

    @Operation(summary = "Get global leaderboard", description = "Returns users ranked by total score with pagination. Includes the requesting user's own rank position.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Leaderboard retrieved successfully")
    })
    @GetMapping
    public ResponseEntity<LeaderboardResponse> getGlobalLeaderboard(
            @AuthenticationPrincipal Jwt jwt,
            @Parameter(description = "Leaderboard type (currently only 'global' supported)") @RequestParam(defaultValue = "global") String type,
            @Parameter(description = "Maximum entries to return (1–100, default 50)") @RequestParam(defaultValue = "50") int limit,
            @Parameter(description = "Pagination offset (0-based)") @RequestParam(defaultValue = "0") int offset) {

        UUID userId = UUID.fromString(jwt.getSubject());
        LeaderboardResponse response = leaderboardService.getGlobalLeaderboard(userId, limit, offset);
        return ResponseEntity.ok(response);
    }
}
