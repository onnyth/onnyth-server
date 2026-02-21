package com.onnyth.onnythserver.controller;

import com.onnyth.onnythserver.dto.BulkStatInputRequest;
import com.onnyth.onnythserver.dto.LifeStatResponse;
import com.onnyth.onnythserver.dto.StatInputRequest;
import com.onnyth.onnythserver.service.LifeStatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/stats")
@RequiredArgsConstructor
@Tag(name = "Life Stats", description = "Life stat input and retrieval APIs")
public class LifeStatController {

    private final LifeStatService lifeStatService;

    @Operation(summary = "Save a single life stat")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Stat saved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid stat value or validation error"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @PostMapping
    public ResponseEntity<LifeStatResponse> saveStat(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody StatInputRequest request) {
        UUID userId = UUID.fromString(jwt.getSubject());
        LifeStatResponse response = lifeStatService.saveStat(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Save multiple life stats (bulk/onboarding)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Stats saved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid stat values or validation error"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @PostMapping("/bulk")
    public ResponseEntity<List<LifeStatResponse>> saveStats(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody BulkStatInputRequest request) {
        UUID userId = UUID.fromString(jwt.getSubject());
        List<LifeStatResponse> responses = lifeStatService.saveStats(userId, request.stats());
        return ResponseEntity.status(HttpStatus.CREATED).body(responses);
    }

    @Operation(summary = "Get all life stats for the current user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Stats retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @GetMapping
    public ResponseEntity<List<LifeStatResponse>> getUserStats(@AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return ResponseEntity.ok(lifeStatService.getUserStats(userId));
    }
}
