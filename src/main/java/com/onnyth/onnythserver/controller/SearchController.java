package com.onnyth.onnythserver.controller;

import com.onnyth.onnythserver.service.SearchService;
import com.onnyth.onnythserver.service.SearchService.LanguageEntry;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Provides autocomplete search endpoints over bundled open datasets.
 * All search is performed in-memory — no external API calls or DB queries.
 * Authentication required (JWT) — same as all other /api/v1 endpoints.
 */
@RestController
@RequestMapping("/api/v1/search")
@RequiredArgsConstructor
@Tag(name = "Search", description = "Autocomplete search for onboarding fields")
public class SearchController {

    private final SearchService searchService;

    @Operation(summary = "Search job roles (O*NET-based taxonomy)")
    @GetMapping("/roles")
    public ResponseEntity<List<String>> searchRoles(
            @RequestParam(required = false, defaultValue = "") String q) {
        return ResponseEntity.ok(searchService.searchRoles(q));
    }

    @Operation(summary = "Search company names")
    @GetMapping("/companies")
    public ResponseEntity<List<String>> searchCompanies(
            @RequestParam(required = false, defaultValue = "") String q) {
        return ResponseEntity.ok(searchService.searchCompanies(q));
    }

    @Operation(summary = "Search university names (Hipo dataset)")
    @GetMapping("/universities")
    public ResponseEntity<List<String>> searchUniversities(
            @RequestParam(required = false, defaultValue = "") String q) {
        return ResponseEntity.ok(searchService.searchUniversities(q));
    }

    @Operation(summary = "Get all available languages (ISO 639-1)")
    @GetMapping("/languages")
    public ResponseEntity<List<LanguageEntry>> getLanguages() {
        return ResponseEntity.ok(searchService.getAllLanguages());
    }
}
