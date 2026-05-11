package com.onnyth.onnythserver.controller;

import com.onnyth.onnythserver.dto.VoteRequest;
import com.onnyth.onnythserver.dto.VoteResponse;
import com.onnyth.onnythserver.service.ProfileVoteService;
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

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/votes")
@RequiredArgsConstructor
@Tag(name = "ProfileVotes", description = "Upvote / downvote on user profiles")
public class ProfileVoteController {

    private final ProfileVoteService voteService;

    @Operation(summary = "Cast or update a vote on a profile")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Vote recorded"),
            @ApiResponse(responseCode = "400", description = "Cannot vote on own profile"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Target user not found")
    })
    @PostMapping
    public ResponseEntity<VoteResponse> castVote(
            @Valid @RequestBody VoteRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        UUID voterId = UUID.fromString(jwt.getSubject());
        return ResponseEntity.ok(voteService.castVote(voterId, request.targetUserId(), request.isUpvote()));
    }

    @Operation(summary = "Remove vote on a profile")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Vote removed (or was already absent)"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Target user not found")
    })
    @DeleteMapping("/{targetUserId}")
    public ResponseEntity<VoteResponse> removeVote(
            @PathVariable UUID targetUserId,
            @AuthenticationPrincipal Jwt jwt) {
        UUID voterId = UUID.fromString(jwt.getSubject());
        return ResponseEntity.ok(voteService.removeVote(voterId, targetUserId));
    }

    @Operation(summary = "Get my current vote on a profile")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Vote status returned (myVote null = no vote)"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Target user not found")
    })
    @GetMapping("/{targetUserId}")
    public ResponseEntity<VoteResponse> getMyVote(
            @PathVariable UUID targetUserId,
            @AuthenticationPrincipal Jwt jwt) {
        UUID voterId = UUID.fromString(jwt.getSubject());
        return ResponseEntity.ok(voteService.getMyVote(voterId, targetUserId));
    }
}
