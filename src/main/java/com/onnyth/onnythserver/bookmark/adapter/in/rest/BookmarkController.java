package com.onnyth.onnythserver.bookmark.adapter.in.rest;

import com.onnyth.onnythserver.bookmark.adapter.in.rest.dto.*;
import com.onnyth.onnythserver.bookmark.application.command.CreateBookmarkCommand;
import com.onnyth.onnythserver.bookmark.application.command.UpdateBookmarkCommand;
import com.onnyth.onnythserver.bookmark.application.usecase.BookmarkUseCaseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/bookmarks")
@RequiredArgsConstructor
@Validated
@Tag(name = "Bookmarks", description = "Bookmark creation and management")
public class BookmarkController {

    private final BookmarkUseCaseService bookmarkUseCaseService;

    @Operation(summary = "Create bookmark", description = "Creates a new bookmark and returns the persisted bookmark details")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Bookmark created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid bookmark request")
    })
    @PostMapping
    public ResponseEntity<BookmarkResponse> create(@Valid @RequestBody BookmarkCreateRequest request) {
        CreateBookmarkCommand command = new CreateBookmarkCommand(
                request.url(),
                request.title(),
                request.tags()
        );

        BookmarkResponse response = BookmarkResponse.fromDomain(
                bookmarkUseCaseService.createBookmark(command)
        );

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.id())
                .toUri();

        return ResponseEntity.created(location).body(response);
    }

    @Operation(summary = "Get bookmark by ID", description = "Retrieves a bookmark by its unique identifier")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Bookmark retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid bookmark ID"),
            @ApiResponse(responseCode = "404", description = "Bookmark not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<BookmarkResponse> getBookmarkById(@PathVariable UUID id) {
        return bookmarkUseCaseService.findBookmarkById(id)
                .map(BookmarkResponse::fromDomain)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @Operation(summary = "Get bookmarks", description = "Returns paginated bookmarks, optionally filtered by an exact tag")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Bookmarks retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid pagination parameters")
    })
    @GetMapping
    public ResponseEntity<BookmarkPageResponse> findByTag(
            @RequestParam(defaultValue = "0") @Min(value = 0, message = "page must be greater than or equal to 0") int page,
            @RequestParam(defaultValue = "20") @Min(value = 1, message = "size must be greater than 0") @Max(value = 100, message = "size must be less than or equal to 100") int size,
            @RequestParam(required = false) String tag
    ) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(BookmarkPageResponse.fromPage(bookmarkUseCaseService.findByTag(tag, pageable)));
    }

    @Operation(summary = "Update bookmark", description = "Updates an existing bookmark by its unique identifier")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Bookmark updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid bookmark ID or request body"),
            @ApiResponse(responseCode = "404", description = "Bookmark not found")
    })
    @PutMapping("/{id}")
    public ResponseEntity<BookmarkResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody BookmarkUpdateRequest request
    ) {
        UpdateBookmarkCommand command = new UpdateBookmarkCommand(
                request.url(),
                request.title(),
                request.tags()
        );

        return ResponseEntity.ok(BookmarkResponse.fromDomain(
                bookmarkUseCaseService.updateBookmark(id, command)
        ));
    }

    @Operation(summary = "Delete bookmark", description = "Deletes a bookmark by its unique identifier")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Bookmark deleted successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid bookmark ID"),
            @ApiResponse(responseCode = "404", description = "Bookmark not found")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable UUID id) {
        bookmarkUseCaseService.deleteBookmark(id);
        return ResponseEntity.noContent().build();
    }
}

