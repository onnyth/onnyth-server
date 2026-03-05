package com.onnyth.onnythserver.controller;

import com.onnyth.onnythserver.dto.CategoryLeaderboardEntryResponse;
import com.onnyth.onnythserver.dto.LeaderboardResponse;
import com.onnyth.onnythserver.dto.UserLeaderboardPositionResponse;
import com.onnyth.onnythserver.models.StatCategory;
import com.onnyth.onnythserver.service.LeaderboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/leaderboard")
@RequiredArgsConstructor
@Tag(name = "Leaderboard", description = "Friends leaderboard, position tracking, and category filters")
public class LeaderboardController {

    private final LeaderboardService leaderboardService;

    @Operation(summary = "Get friends leaderboard", description = "Returns paginated leaderboard of friends + self, sorted by total score (or filtered by category)")
    @GetMapping
    public ResponseEntity<?> getLeaderboard(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) StatCategory category,
            @AuthenticationPrincipal Jwt jwt) {

        UUID userId = UUID.fromString(jwt.getSubject());
        size = Math.min(size, 50);
        PageRequest pageable = PageRequest.of(page, size);

        if (category != null) {
            Page<CategoryLeaderboardEntryResponse> result = leaderboardService.getLeaderboardByCategory(userId,
                    category, pageable);
            return ResponseEntity.ok(result);
        }

        LeaderboardResponse result = leaderboardService.getFriendsLeaderboard(userId, pageable);
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "Get current user's leaderboard position", description = "Returns position among friends, points gap to next position, and user ahead")
    @GetMapping("/my-position")
    public ResponseEntity<UserLeaderboardPositionResponse> getMyPosition(
            @AuthenticationPrincipal Jwt jwt) {

        UUID userId = UUID.fromString(jwt.getSubject());
        return ResponseEntity.ok(leaderboardService.getUserPosition(userId));
    }
}
