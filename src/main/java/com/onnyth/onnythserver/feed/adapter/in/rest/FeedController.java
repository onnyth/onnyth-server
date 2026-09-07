package com.onnyth.onnythserver.feed.adapter.in.rest;

import com.onnyth.onnythserver.feed.adapter.in.rest.dto.FeedEventResponse;
import com.onnyth.onnythserver.feed.application.usecase.FeedUseCaseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
@RequestMapping("/api/v1/feed")
@RequiredArgsConstructor
@Tag(name = "Feed", description = "Social activity feed — see what friends are doing")
public class FeedController {

    private final FeedUseCaseService feedService;

    @Operation(summary = "Get friend feed", description = "Returns paginated feed of friend activities, level-ups, achievements, and streaks")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Feed retrieved")
    })
    @GetMapping
    public ResponseEntity<Page<FeedEventResponse>> getFeed(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return ResponseEntity.ok(feedService.getFriendFeed(userId, PageRequest.of(page, size)));
    }
}
