package com.onnyth.onnythserver.bookmark.adapter.in.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.onnyth.onnythserver.bookmark.adapter.in.rest.dto.CreateBookmarkRequest;
import com.onnyth.onnythserver.bookmark.adapter.in.rest.dto.BookmarkUpdateRequest;
import com.onnyth.onnythserver.bookmark.adapter.in.rest.dto.CreateBookmarkResponse;
import com.onnyth.onnythserver.bookmark.application.command.CreateBookmarkCommand;
import com.onnyth.onnythserver.bookmark.application.command.UpdateBookmarkCommand;
import com.onnyth.onnythserver.bookmark.application.exception.BookmarkNotFoundException;
import com.onnyth.onnythserver.bookmark.application.usecase.BookmarkUseCaseService;
import com.onnyth.onnythserver.bookmark.domain.model.Bookmark;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BookmarkController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(BookmarkExceptionHandler.class)
@DisplayName("BookmarkController")
class BookmarkControllerTest {

    private static final UUID BOOKMARK_ID = UUID.fromString("00000000-0000-0000-0000-000000000111");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private BookmarkUseCaseService bookmarkUseCaseService;

    @Nested
    @DisplayName("POST /api/v1/bookmarks")
    class Create {

        @Test
        @DisplayName("returns 201 with bookmark response and location header")
        void returns201OnSuccess() throws Exception {
            CreateBookmarkRequest request = new CreateBookmarkRequest(
                    "https://onnyth.com/article",
                    "Useful article",
                    Set.of("wellness", "productivity")
            );

            Bookmark bookmark = Bookmark.builder()
                    .id(BOOKMARK_ID)
                    .url(request.url())
                    .title(request.title())
                    .tags(request.tags())
                    .createdAt(Instant.parse("2026-08-08T10:15:30Z"))
                    .updatedAt(Instant.parse("2026-08-08T10:15:30Z"))
                    .build();

            when(bookmarkUseCaseService.createBookmark(any(CreateBookmarkCommand.class), anyString())).thenReturn(CreateBookmarkResponse.fromDomain(bookmark));

            mockMvc.perform(post("/api/v1/bookmarks")
                            .contentType(MediaType.APPLICATION_JSON)
                            .header("Idempotency-Key", "11111111-1111-1111-1111-111111111111")
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(header().string("Location", "http://localhost/api/v1/bookmarks/" + BOOKMARK_ID))
                    .andExpect(jsonPath("$.id").value(BOOKMARK_ID.toString()))
                    .andExpect(jsonPath("$.url").value("https://onnyth.com/article"))
                    .andExpect(jsonPath("$.title").value("Useful article"));
        }

        @Test
        @DisplayName("returns 400 when Idempotency-Key header is missing")
        void returns400WhenIdempotencyKeyMissing() throws Exception {
            CreateBookmarkRequest request = new CreateBookmarkRequest(
                    "https://onnyth.com/article",
                    "Useful article",
                    Set.of("wellness", "productivity")
            );

            mockMvc.perform(post("/api/v1/bookmarks")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.message").value("Idempotency-Key header is required"));

            verifyNoInteractions(bookmarkUseCaseService);
        }

        @Test
        @DisplayName("returns 400 when request payload is invalid")
        void returns400OnInvalidRequest() throws Exception {
            CreateBookmarkRequest request = new CreateBookmarkRequest(
                    "not-a-url",
                    "Useful article",
                    Set.of("wellness")
            );

            mockMvc.perform(post("/api/v1/bookmarks")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.error").value("Validation Error"))
                    .andExpect(jsonPath("$.message").value("url: must be a valid URI"));

            verifyNoInteractions(bookmarkUseCaseService);
        }

        @Test
        @DisplayName("returns 400 when title is missing")
        void returns400WhenTitleMissing() throws Exception {
            String payload = """
                    {
                      "url": "https://onnyth.com/article",
                      "tags": ["wellness"]
                    }
                    """;

            mockMvc.perform(post("/api/v1/bookmarks")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(payload))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("title: Title is required"));

            verifyNoInteractions(bookmarkUseCaseService);
        }

        @Test
        @DisplayName("returns 400 when title exceeds max length")
        void returns400WhenTitleTooLong() throws Exception {
            CreateBookmarkRequest request = new CreateBookmarkRequest(
                    "https://onnyth.com/article",
                    "a".repeat(256),
                    Set.of("wellness")
            );

            mockMvc.perform(post("/api/v1/bookmarks")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("title: must not exceed 255 characters"));

            verifyNoInteractions(bookmarkUseCaseService);
        }

        @Test
        @DisplayName("returns 400 when a tag exceeds max length")
        void returns400WhenTagTooLong() throws Exception {
            CreateBookmarkRequest request = new CreateBookmarkRequest(
                    "https://onnyth.com/article",
                    "Useful article",
                    Set.of("a".repeat(51))
            );

            mockMvc.perform(post("/api/v1/bookmarks")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("tag: must not exceed 50 characters"));

            verifyNoInteractions(bookmarkUseCaseService);
        }

        @Test
        @DisplayName("accepts omitted tags and still creates bookmark")
        void acceptsOmittedTags() throws Exception {
            String payload = """
                    {
                      "url": "https://onnyth.com/article",
                      "title": "Useful article"
                    }
                    """;

            Bookmark bookmark = Bookmark.builder()
                    .id(BOOKMARK_ID)
                    .url("https://onnyth.com/article")
                    .title("Useful article")
                    .tags(Set.of())
                    .createdAt(Instant.parse("2026-08-08T10:15:30Z"))
                    .updatedAt(Instant.parse("2026-08-08T10:15:30Z"))
                    .build();

            when(bookmarkUseCaseService.createBookmark(any(CreateBookmarkCommand.class), anyString())).thenReturn(CreateBookmarkResponse.fromDomain(bookmark));

            mockMvc.perform(post("/api/v1/bookmarks")
                            .contentType(MediaType.APPLICATION_JSON)
                            .header("Idempotency-Key", "22222222-2222-2222-2222-222222222222")
                            .content(payload))
                    .andExpect(status().isCreated());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/bookmarks/{id}")
    class GetById {

        @Test
        @DisplayName("returns 200 when bookmark exists")
        void returns200WhenBookmarkExists() throws Exception {
            Bookmark bookmark = Bookmark.builder()
                    .id(BOOKMARK_ID)
                    .url("https://onnyth.com/article")
                    .title("Useful article")
                    .tags(Set.of("wellness"))
                    .createdAt(Instant.parse("2026-08-08T10:15:30Z"))
                    .updatedAt(Instant.parse("2026-08-08T10:15:30Z"))
                    .build();

            when(bookmarkUseCaseService.findBookmarkById(BOOKMARK_ID)).thenReturn(Optional.of(bookmark));

            mockMvc.perform(get("/api/v1/bookmarks/{id}", BOOKMARK_ID))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(BOOKMARK_ID.toString()));
        }

        @Test
        @DisplayName("returns 404 when bookmark does not exist")
        void returns404WhenBookmarkMissing() throws Exception {
            when(bookmarkUseCaseService.findBookmarkById(BOOKMARK_ID)).thenReturn(Optional.empty());

            mockMvc.perform(get("/api/v1/bookmarks/{id}", BOOKMARK_ID))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/bookmarks")
    class GetBookmarks {

        @Test
        @DisplayName("returns default paginated bookmarks when no query params are provided")
        void returnsDefaultPagination() throws Exception {
            Bookmark bookmark = Bookmark.builder()
                    .id(BOOKMARK_ID)
                    .url("https://onnyth.com/article")
                    .title("Useful article")
                    .tags(Set.of("learning"))
                    .createdAt(Instant.parse("2026-08-08T10:15:30Z"))
                    .updatedAt(Instant.parse("2026-08-08T10:15:30Z"))
                    .build();

            Page<Bookmark> page = new PageImpl<>(
                    List.of(bookmark),
                    PageRequest.of(0, 20),
                    1
            );

            when(bookmarkUseCaseService.findByTag(eq(null), any(Pageable.class))).thenReturn(page);

            mockMvc.perform(get("/api/v1/bookmarks"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[0].id").value(BOOKMARK_ID.toString()))
                    .andExpect(jsonPath("$.totalElements").value(1))
                    .andExpect(jsonPath("$.totalPages").value(1))
                    .andExpect(jsonPath("$.currentPage").value(0))
                    .andExpect(jsonPath("$.hasNext").value(false));
        }

        @Test
        @DisplayName("returns filtered paginated bookmarks by exact tag")
        void returnsFilteredByTag() throws Exception {
            Bookmark first = Bookmark.builder()
                    .id(UUID.fromString("00000000-0000-0000-0000-000000000112"))
                    .url("https://onnyth.com/a").title("A").tags(Set.of("learning"))
                    .createdAt(Instant.parse("2026-08-08T10:15:30Z"))
                    .updatedAt(Instant.parse("2026-08-08T10:15:30Z"))
                    .build();
            Bookmark second = Bookmark.builder()
                    .id(UUID.fromString("00000000-0000-0000-0000-000000000113"))
                    .url("https://onnyth.com/b").title("B").tags(Set.of("learning"))
                    .createdAt(Instant.parse("2026-08-08T10:15:30Z"))
                    .updatedAt(Instant.parse("2026-08-08T10:15:30Z"))
                    .build();

            Page<Bookmark> page = new PageImpl<>(List.of(first, second), PageRequest.of(1, 5), 12);

            when(bookmarkUseCaseService.findByTag(eq("learning"), any(Pageable.class))).thenReturn(page);

            mockMvc.perform(get("/api/v1/bookmarks")
                            .param("page", "1").param("size", "5").param("tag", "learning"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content.length()").value(2))
                    .andExpect(jsonPath("$.totalElements").value(12))
                    .andExpect(jsonPath("$.totalPages").value(3))
                    .andExpect(jsonPath("$.currentPage").value(1))
                    .andExpect(jsonPath("$.hasNext").value(true));
        }

        @Test
        @DisplayName("returns 400 for negative page")
        void returns400ForNegativePage() throws Exception {
            mockMvc.perform(get("/api/v1/bookmarks").param("page", "-1"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400));

            verifyNoInteractions(bookmarkUseCaseService);
        }

        @Test
        @DisplayName("returns 400 for invalid size <= 0")
        void returns400ForNonPositiveSize() throws Exception {
            mockMvc.perform(get("/api/v1/bookmarks").param("size", "0"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400));

            verifyNoInteractions(bookmarkUseCaseService);
        }

        @Test
        @DisplayName("returns 400 for size above cap")
        void returns400ForTooLargeSize() throws Exception {
            mockMvc.perform(get("/api/v1/bookmarks").param("size", "101"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400));

            verifyNoInteractions(bookmarkUseCaseService);
        }
    }

    @Nested
    @DisplayName("PUT /api/v1/bookmarks/{id}")
    class Update {

        @Test
        @DisplayName("returns 200 with updated bookmark response")
        void returns200OnSuccess() throws Exception {
            BookmarkUpdateRequest request = new BookmarkUpdateRequest(
                    "https://updated-example.com",
                    "Updated Title",
                    Set.of("reference", "important")
            );

            Bookmark updated = Bookmark.builder()
                    .id(BOOKMARK_ID)
                    .url(request.url())
                    .title(request.title())
                    .tags(request.tags())
                    .createdAt(Instant.parse("2026-08-08T10:15:30Z"))
                    .updatedAt(Instant.parse("2026-08-09T10:15:30Z"))
                    .build();

            when(bookmarkUseCaseService.updateBookmark(eq(BOOKMARK_ID), any(UpdateBookmarkCommand.class))).thenReturn(updated);

            mockMvc.perform(put("/api/v1/bookmarks/{id}", BOOKMARK_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(BOOKMARK_ID.toString()))
                    .andExpect(jsonPath("$.url").value("https://updated-example.com"))
                    .andExpect(jsonPath("$.title").value("Updated Title"))
                    .andExpect(jsonPath("$.createdAt").value("2026-08-08T10:15:30Z"))
                    .andExpect(jsonPath("$.updatedAt").value("2026-08-09T10:15:30Z"));
        }

        @Test
        @DisplayName("returns 404 when bookmark does not exist")
        void returns404WhenBookmarkMissing() throws Exception {
            BookmarkUpdateRequest request = new BookmarkUpdateRequest(
                    "https://updated-example.com", "Updated Title", Set.of("reference")
            );

            when(bookmarkUseCaseService.updateBookmark(eq(BOOKMARK_ID), any(UpdateBookmarkCommand.class)))
                    .thenThrow(new BookmarkNotFoundException(BOOKMARK_ID.toString()));

            mockMvc.perform(put("/api/v1/bookmarks/{id}", BOOKMARK_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status").value(404))
                    .andExpect(jsonPath("$.error").value("Not Found"));
        }

        @Test
        @DisplayName("returns 400 when update payload is invalid")
        void returns400WhenPayloadInvalid() throws Exception {
            BookmarkUpdateRequest request = new BookmarkUpdateRequest(
                    "bad-url", "Updated Title", Set.of("reference")
            );

            mockMvc.perform(put("/api/v1/bookmarks/{id}", BOOKMARK_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("url: must be a valid URI"));

            verifyNoInteractions(bookmarkUseCaseService);
        }
    }

    @Nested
    @DisplayName("DELETE /api/v1/bookmarks/{id}")
    class Delete {

        @Test
        @DisplayName("returns 204 when bookmark is deleted")
        void returns204OnSuccess() throws Exception {
            doNothing().when(bookmarkUseCaseService).deleteBookmark(BOOKMARK_ID);

            mockMvc.perform(delete("/api/v1/bookmarks/{id}", BOOKMARK_ID))
                    .andExpect(status().isNoContent());
        }

        @Test
        @DisplayName("returns 404 when bookmark does not exist")
        void returns404WhenBookmarkMissing() throws Exception {
            doThrow(new BookmarkNotFoundException(BOOKMARK_ID.toString()))
                    .when(bookmarkUseCaseService).deleteBookmark(BOOKMARK_ID);

            mockMvc.perform(delete("/api/v1/bookmarks/{id}", BOOKMARK_ID))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status").value(404))
                    .andExpect(jsonPath("$.error").value("Not Found"));
        }

        @Test
        @DisplayName("returns 400 when bookmark id is malformed")
        void returns400WhenIdMalformed() throws Exception {
            mockMvc.perform(delete("/api/v1/bookmarks/{id}", "bad-id"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.error").value("Validation Error"))
                    .andExpect(jsonPath("$.message").value("id has invalid format"));

            verifyNoInteractions(bookmarkUseCaseService);
        }
    }
}

