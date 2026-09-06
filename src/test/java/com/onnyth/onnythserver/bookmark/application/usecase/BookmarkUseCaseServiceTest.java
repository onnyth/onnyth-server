package com.onnyth.onnythserver.bookmark.application.usecase;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.onnyth.onnythserver.bookmark.adapter.in.rest.dto.CreateBookmarkResponse;
import com.onnyth.onnythserver.bookmark.application.command.CreateBookmarkCommand;
import com.onnyth.onnythserver.bookmark.application.exception.IdempotencyConflictException;
import com.onnyth.onnythserver.bookmark.application.port.BookmarkRepository;
import com.onnyth.onnythserver.bookmark.domain.model.Bookmark;
import com.onnyth.onnythserver.shared.idempotency.application.IdempotencyResponse;
import com.onnyth.onnythserver.shared.idempotency.application.IdempotencySerializer;
import com.onnyth.onnythserver.shared.idempotency.application.IdempotencyService;
import com.onnyth.onnythserver.shared.utils.RequestHasher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("BookmarkUseCaseService")
class BookmarkUseCaseServiceTest {

    private static final UUID BOOKMARK_ID = UUID.fromString("00000000-0000-0000-0000-000000000111");

    @Mock
    private BookmarkRepository bookmarkRepository;

    @Mock
    private IdempotencyService idempotencyService;

    private IdempotencySerializer idempotencySerializer;
    private RequestHasher requestHasher;

    private BookmarkUseCaseService bookmarkUseCaseService;

    @BeforeEach
    void setUp() {
        idempotencySerializer = new IdempotencySerializer(new ObjectMapper().findAndRegisterModules());
        requestHasher = new RequestHasher();
        bookmarkUseCaseService = new BookmarkUseCaseService(
                bookmarkRepository,
                idempotencyService,
                idempotencySerializer,
                requestHasher
        );
    }

    private Bookmark persistedBookmark(CreateBookmarkCommand command) {
        return Bookmark.builder()
                .id(BOOKMARK_ID)
                .url(command.url())
                .title(command.title())
                .tags(command.tags() == null ? Set.of() : command.tags())
                .createdAt(Instant.parse("2026-08-08T10:15:30Z"))
                .updatedAt(Instant.parse("2026-08-08T10:15:30Z"))
                .build();
    }

    @Nested
    @DisplayName("createBookmark")
    class CreateBookmark {

        @Test
        @DisplayName("creates and returns bookmark on cache miss, then caches the response")
        void createsBookmarkOnCacheMiss() {
            CreateBookmarkCommand command = CreateBookmarkCommand.of(
                    "https://onnyth.com/article", "Useful article", Set.of("wellness")
            );
            String idempotencyKey = "key-1";

            when(idempotencyService.get(idempotencyKey)).thenReturn(Optional.empty());
            when(bookmarkRepository.save(any(Bookmark.class))).thenReturn(persistedBookmark(command));

            CreateBookmarkResponse response = bookmarkUseCaseService.createBookmark(command, idempotencyKey);

            assertThat(response.id()).isEqualTo(BOOKMARK_ID);
            assertThat(response.url()).isEqualTo(command.url());
            verify(idempotencyService).save(anyString(), any(IdempotencyResponse.class));
        }

        @Test
        @DisplayName("replays cached response when the same key and body are retried")
        void replaysCachedResponseOnMatchingRetry() {
            CreateBookmarkCommand command = CreateBookmarkCommand.of(
                    "https://onnyth.com/article", "Useful article", Set.of("wellness")
            );
            String idempotencyKey = "key-2";

            CreateBookmarkResponse cachedResponse = CreateBookmarkResponse.fromDomain(persistedBookmark(command));
            String requestHash = requestHasher.hash(command.url() + "|" + command.title() + "|wellness");
            IdempotencyResponse cachedRecord = idempotencySerializer.serialize(requestHash, cachedResponse);

            when(idempotencyService.get(idempotencyKey)).thenReturn(Optional.of(cachedRecord));

            CreateBookmarkResponse response = bookmarkUseCaseService.createBookmark(command, idempotencyKey);

            assertThat(response.id()).isEqualTo(BOOKMARK_ID);
            verify(bookmarkRepository, never()).save(any(Bookmark.class));
        }

        @Test
        @DisplayName("throws IdempotencyConflictException when the same key is reused with a different body")
        void throwsConflictOnDifferentBody() {
            CreateBookmarkCommand originalCommand = CreateBookmarkCommand.of(
                    "https://onnyth.com/article", "Useful article", Set.of("wellness")
            );
            CreateBookmarkCommand retriedCommand = CreateBookmarkCommand.of(
                    "https://onnyth.com/different-article", "Different title", Set.of("wellness")
            );
            String idempotencyKey = "key-3";

            CreateBookmarkResponse cachedResponse = CreateBookmarkResponse.fromDomain(persistedBookmark(originalCommand));
            String originalHash = requestHasher.hash(originalCommand.url() + "|" + originalCommand.title() + "|wellness");
            IdempotencyResponse cachedRecord = idempotencySerializer.serialize(originalHash, cachedResponse);

            when(idempotencyService.get(idempotencyKey)).thenReturn(Optional.of(cachedRecord));

            assertThatThrownBy(() -> bookmarkUseCaseService.createBookmark(retriedCommand, idempotencyKey))
                    .isInstanceOf(IdempotencyConflictException.class);

            verify(bookmarkRepository, never()).save(any(Bookmark.class));
        }

        @Test
        @DisplayName("treats tag ordering as irrelevant to the idempotency hash")
        void hashIsStableRegardlessOfTagOrder() {
            CreateBookmarkCommand original = CreateBookmarkCommand.of(
                    "https://onnyth.com/article", "Useful article", Set.of("wellness", "productivity")
            );
            CreateBookmarkCommand retried = CreateBookmarkCommand.of(
                    "https://onnyth.com/article", "Useful article", Set.of("productivity", "wellness")
            );
            String idempotencyKey = "key-4";

            CreateBookmarkResponse cachedResponse = CreateBookmarkResponse.fromDomain(persistedBookmark(original));
            String canonicalHash = requestHasher.hash(original.url() + "|" + original.title() + "|productivity,wellness");
            IdempotencyResponse cachedRecord = idempotencySerializer.serialize(canonicalHash, cachedResponse);

            when(idempotencyService.get(idempotencyKey)).thenReturn(Optional.of(cachedRecord));

            CreateBookmarkResponse response = bookmarkUseCaseService.createBookmark(retried, idempotencyKey);

            assertThat(response.id()).isEqualTo(BOOKMARK_ID);
            verify(bookmarkRepository, never()).save(any(Bookmark.class));
        }

        @Test
        @DisplayName("still creates and returns the bookmark when the idempotency cache read fails")
        void createsBookmarkWhenCacheReadFails() {
            CreateBookmarkCommand command = CreateBookmarkCommand.of(
                    "https://onnyth.com/article", "Useful article", Set.of("wellness")
            );
            String idempotencyKey = "key-5";

            when(idempotencyService.get(idempotencyKey)).thenThrow(new RuntimeException("Redis unavailable"));
            when(bookmarkRepository.save(any(Bookmark.class))).thenReturn(persistedBookmark(command));

            CreateBookmarkResponse response = bookmarkUseCaseService.createBookmark(command, idempotencyKey);

            assertThat(response.id()).isEqualTo(BOOKMARK_ID);
            verify(bookmarkRepository).save(any(Bookmark.class));
        }

        @Test
        @DisplayName("still returns the created bookmark when the idempotency cache write fails")
        void returnsBookmarkWhenCacheWriteFails() {
            CreateBookmarkCommand command = CreateBookmarkCommand.of(
                    "https://onnyth.com/article", "Useful article", Set.of("wellness")
            );
            String idempotencyKey = "key-6";

            when(idempotencyService.get(idempotencyKey)).thenReturn(Optional.empty());
            when(bookmarkRepository.save(any(Bookmark.class))).thenReturn(persistedBookmark(command));
            doThrow(new RuntimeException("Redis unavailable"))
                    .when(idempotencyService).save(anyString(), any(IdempotencyResponse.class));

            CreateBookmarkResponse response = bookmarkUseCaseService.createBookmark(command, idempotencyKey);

            assertThat(response.id()).isEqualTo(BOOKMARK_ID);
            assertThat(response.url()).isEqualTo(command.url());
        }
    }
}
