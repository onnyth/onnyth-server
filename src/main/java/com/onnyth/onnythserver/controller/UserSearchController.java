package com.onnyth.onnythserver.controller;

import com.onnyth.onnythserver.dto.UserSearchResponse;
import com.onnyth.onnythserver.service.UserSearchService;
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
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "User Search", description = "Search for users by username or name")
public class UserSearchController {

    private final UserSearchService userSearchService;

    @Operation(summary = "Search users", description = "Case-insensitive partial match on username or full name, excludes self, annotates friendship status")
    @GetMapping("/search")
    public ResponseEntity<Page<UserSearchResponse>> searchUsers(
            @RequestParam String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal Jwt jwt) {

        UUID userId = UUID.fromString(jwt.getSubject());
        size = Math.min(size, 50);
        Page<UserSearchResponse> results = userSearchService.searchUsers(q, userId, PageRequest.of(page, size));
        return ResponseEntity.ok(results);
    }
}
