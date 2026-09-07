package com.onnyth.onnythserver.quest.adapter.in.rest;

import com.onnyth.onnythserver.quest.adapter.in.rest.dto.QuestCompletionResponse;
import com.onnyth.onnythserver.quest.adapter.in.rest.dto.QuestListResponse;
import com.onnyth.onnythserver.quest.adapter.in.rest.dto.QuestResponse;
import com.onnyth.onnythserver.quest.application.usecase.QuestUseCaseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/quests")
@RequiredArgsConstructor
@Tag(name = "Quests", description = "Quest system — view and complete quests to earn XP")
public class QuestController {

    private final QuestUseCaseService questService;

    @Operation(summary = "Get active quests", description = "Returns all active quests with the user's completion status")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Active quests retrieved")
    })
    @GetMapping("/active")
    public ResponseEntity<QuestListResponse> getActiveQuests(@AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return ResponseEntity.ok(questService.getActiveQuests(userId));
    }

    @Operation(summary = "Get quest details", description = "Returns a single quest by ID with completion status")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Quest retrieved"),
            @ApiResponse(responseCode = "404", description = "Quest not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<QuestResponse> getQuestById(
            @PathVariable UUID id,
            @AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return ResponseEntity.ok(questService.getQuestById(id, userId));
    }

    @Operation(summary = "Complete a quest", description = "Marks a quest as completed and awards XP to the user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Quest completed, XP awarded"),
            @ApiResponse(responseCode = "400", description = "Quest has expired"),
            @ApiResponse(responseCode = "404", description = "Quest not found or not active"),
            @ApiResponse(responseCode = "409", description = "Quest already completed")
    })
    @PostMapping("/{id}/complete")
    public ResponseEntity<QuestCompletionResponse> completeQuest(
            @PathVariable UUID id,
            @AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return ResponseEntity.ok(questService.completeQuest(userId, id));
    }
}
