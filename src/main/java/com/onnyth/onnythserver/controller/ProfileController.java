package com.onnyth.onnythserver.controller;

import com.onnyth.onnythserver.dto.ProfileCardResponse;
import com.onnyth.onnythserver.dto.ProfileResponse;
import com.onnyth.onnythserver.dto.ProfileUpdateRequest;
import com.onnyth.onnythserver.dto.RankProgressResponse;
import com.onnyth.onnythserver.service.ProfileService;
import com.onnyth.onnythserver.service.RankService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/profile")
@RequiredArgsConstructor
@Tag(name = "Profile", description = "User profile management APIs")
public class ProfileController {

        private final ProfileService profileService;
        private final RankService rankService;

        @Operation(summary = "Get current user's profile")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Profile retrieved successfully"),
                        @ApiResponse(responseCode = "401", description = "Unauthorized"),
                        @ApiResponse(responseCode = "404", description = "User not found")
        })
        @GetMapping
        public ResponseEntity<ProfileResponse> getProfile(@AuthenticationPrincipal Jwt jwt) {
                UUID userId = UUID.fromString(jwt.getSubject());
                return ResponseEntity.ok(profileService.getProfile(userId));
        }

        @Operation(summary = "Update current user's profile")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Profile updated successfully"),
                        @ApiResponse(responseCode = "400", description = "Invalid request data"),
                        @ApiResponse(responseCode = "401", description = "Unauthorized"),
                        @ApiResponse(responseCode = "404", description = "User not found"),
                        @ApiResponse(responseCode = "409", description = "Username already exists")
        })
        @PutMapping
        public ResponseEntity<ProfileResponse> updateProfile(
                        @AuthenticationPrincipal Jwt jwt,
                        @Valid @RequestBody ProfileUpdateRequest request) {
                UUID userId = UUID.fromString(jwt.getSubject());
                return ResponseEntity.ok(profileService.updateProfile(userId, request));
        }

        @Operation(summary = "Check if a username is available")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Username availability checked")
        })
        @GetMapping("/check-username/{username}")
        public ResponseEntity<Map<String, Object>> checkUsernameAvailability(
                        @AuthenticationPrincipal Jwt jwt,
                        @PathVariable String username) {
                UUID userId = jwt != null ? UUID.fromString(jwt.getSubject()) : null;
                boolean available = profileService.isUsernameAvailable(username, userId);
                return ResponseEntity.ok(Map.of(
                                "username", username,
                                "available", available));
        }

        @Operation(summary = "Upload profile picture")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Profile picture uploaded successfully"),
                        @ApiResponse(responseCode = "400", description = "Invalid file"),
                        @ApiResponse(responseCode = "401", description = "Unauthorized"),
                        @ApiResponse(responseCode = "404", description = "User not found")
        })
        @PostMapping(value = "/picture", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
        public ResponseEntity<ProfileResponse> uploadProfilePicture(
                        @AuthenticationPrincipal Jwt jwt,
                        @RequestParam("file") MultipartFile file) throws IOException {
                UUID userId = UUID.fromString(jwt.getSubject());

                return ResponseEntity.ok(profileService.uploadProfilePicture(
                                userId,
                                file.getBytes(),
                                file.getContentType(),
                                file.getOriginalFilename()));
        }

        @Operation(summary = "Get current user's profile card")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Profile card retrieved successfully"),
                        @ApiResponse(responseCode = "401", description = "Unauthorized"),
                        @ApiResponse(responseCode = "404", description = "User not found")
        })
        @GetMapping("/card")
        public ResponseEntity<ProfileCardResponse> getProfileCard(@AuthenticationPrincipal Jwt jwt) {
                UUID userId = UUID.fromString(jwt.getSubject());
                return ResponseEntity.ok(profileService.getProfileCard(userId));
        }

        @Operation(summary = "Get another user's profile card (viewer mode)")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Profile card retrieved successfully"),
                        @ApiResponse(responseCode = "401", description = "Unauthorized"),
                        @ApiResponse(responseCode = "404", description = "User not found")
        })
        @GetMapping("/{userId}/card")
        public ResponseEntity<ProfileCardResponse> getPublicProfileCard(
                        @AuthenticationPrincipal Jwt jwt,
                        @PathVariable UUID userId) {
                // onnythCoins is intentionally omitted by client when in viewer mode
                return ResponseEntity.ok(profileService.getProfileCard(userId));
        }

        @Operation(summary = "Set active background color")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Background color updated"),
                        @ApiResponse(responseCode = "400", description = "Invalid hex color"),
                        @ApiResponse(responseCode = "401", description = "Unauthorized")
        })
        @PutMapping("/background-color")
        public ResponseEntity<ProfileCardResponse> setBackgroundColor(
                        @AuthenticationPrincipal Jwt jwt,
                        @RequestBody Map<String, String> body) {
                UUID userId = UUID.fromString(jwt.getSubject());
                String hex = body.getOrDefault("color", "").trim();
                if (!hex.matches("#[0-9A-Fa-f]{6}")) {
                        return ResponseEntity.badRequest().build();
                }
                return ResponseEntity.ok(profileService.setActiveBackgroundColor(userId, hex));
        }

        @Operation(summary = "Get current user's rank progress")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Rank progress retrieved successfully"),
                        @ApiResponse(responseCode = "401", description = "Unauthorized"),
                        @ApiResponse(responseCode = "404", description = "User not found")
        })
        @GetMapping("/rank")
        public ResponseEntity<RankProgressResponse> getRankProgress(@AuthenticationPrincipal Jwt jwt) {
                UUID userId = UUID.fromString(jwt.getSubject());
                return ResponseEntity.ok(rankService.getRankProgress(userId));
        }
}
