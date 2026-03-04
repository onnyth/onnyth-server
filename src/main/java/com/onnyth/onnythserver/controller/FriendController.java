package com.onnyth.onnythserver.controller;

import com.onnyth.onnythserver.dto.FriendProfileResponse;
import com.onnyth.onnythserver.dto.FriendRequestResponse;
import com.onnyth.onnythserver.dto.FriendResponse;
import com.onnyth.onnythserver.service.FriendshipService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/friends")
@RequiredArgsConstructor
@Tag(name = "Friends", description = "Friend requests, friends list, and friend profiles")
public class FriendController {

    private final FriendshipService friendshipService;

    // ─── Friend Requests ──────────────────────────────────────────────────────

    @Operation(summary = "Send friend request")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Friend request sent"),
            @ApiResponse(responseCode = "409", description = "Already friends or duplicate request")
    })
    @PostMapping("/request/{userId}")
    public ResponseEntity<FriendRequestResponse> sendFriendRequest(
            @PathVariable UUID userId,
            @AuthenticationPrincipal Jwt jwt) {
        UUID senderId = UUID.fromString(jwt.getSubject());
        return ResponseEntity.ok(friendshipService.sendFriendRequest(senderId, userId));
    }

    @Operation(summary = "Get received friend requests")
    @GetMapping("/requests/received")
    public ResponseEntity<List<FriendRequestResponse>> getReceivedRequests(
            @AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return ResponseEntity.ok(friendshipService.getReceivedRequests(userId));
    }

    @Operation(summary = "Get sent friend requests")
    @GetMapping("/requests/sent")
    public ResponseEntity<List<FriendRequestResponse>> getSentRequests(
            @AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return ResponseEntity.ok(friendshipService.getSentRequests(userId));
    }

    @Operation(summary = "Get pending request count (for badge)")
    @GetMapping("/requests/count")
    public ResponseEntity<Map<String, Long>> getPendingRequestCount(
            @AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getSubject());
        long count = friendshipService.getPendingRequestCount(userId);
        return ResponseEntity.ok(Map.of("count", count));
    }

    @Operation(summary = "Accept friend request")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Friend request accepted"),
            @ApiResponse(responseCode = "403", description = "Only receiver can accept"),
            @ApiResponse(responseCode = "404", description = "Friend request not found")
    })
    @PutMapping("/request/{requestId}/accept")
    public ResponseEntity<FriendRequestResponse> acceptFriendRequest(
            @PathVariable UUID requestId,
            @AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return ResponseEntity.ok(friendshipService.acceptFriendRequest(requestId, userId));
    }

    @Operation(summary = "Reject friend request")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Friend request rejected"),
            @ApiResponse(responseCode = "403", description = "Only receiver can reject"),
            @ApiResponse(responseCode = "404", description = "Friend request not found")
    })
    @PutMapping("/request/{requestId}/reject")
    public ResponseEntity<FriendRequestResponse> rejectFriendRequest(
            @PathVariable UUID requestId,
            @AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return ResponseEntity.ok(friendshipService.rejectFriendRequest(requestId, userId));
    }

    // ─── Friends List ─────────────────────────────────────────────────────────

    @Operation(summary = "Get friends list (paginated)")
    @GetMapping
    public ResponseEntity<Page<FriendResponse>> getFriends(
            @RequestParam(defaultValue = "createdAt") String sort,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getSubject());
        size = Math.min(size, 50);
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, sort));
        return ResponseEntity.ok(friendshipService.getFriends(userId, pageRequest));
    }

    @Operation(summary = "Search within friends")
    @GetMapping("/search")
    public ResponseEntity<List<FriendResponse>> searchFriends(
            @RequestParam String q,
            @AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return ResponseEntity.ok(friendshipService.searchFriends(userId, q));
    }

    @Operation(summary = "Remove a friend")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Friend removed"),
            @ApiResponse(responseCode = "400", description = "Not friends")
    })
    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> removeFriend(
            @PathVariable UUID userId,
            @AuthenticationPrincipal Jwt jwt) {
        UUID currentUserId = UUID.fromString(jwt.getSubject());
        friendshipService.removeFriend(currentUserId, userId);
        return ResponseEntity.noContent().build();
    }

    // ─── Friend Profile ───────────────────────────────────────────────────────

    @Operation(summary = "Get friend's profile with stat comparison")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Friend profile with comparison"),
            @ApiResponse(responseCode = "400", description = "Not friends")
    })
    @GetMapping("/{userId}/profile")
    public ResponseEntity<FriendProfileResponse> getFriendProfile(
            @PathVariable UUID userId,
            @AuthenticationPrincipal Jwt jwt) {
        UUID currentUserId = UUID.fromString(jwt.getSubject());
        return ResponseEntity.ok(friendshipService.getFriendProfile(currentUserId, userId));
    }
}
