package com.onnyth.onnythserver.bookmark.application.usecase;

import com.onnyth.onnythserver.bookmark.adapter.in.rest.dto.CreateBookmarkResponse;
import com.onnyth.onnythserver.bookmark.application.command.CreateBookmarkCommand;
import com.onnyth.onnythserver.bookmark.application.command.UpdateBookmarkCommand;
import com.onnyth.onnythserver.bookmark.application.exception.BookmarkNotFoundException;
import com.onnyth.onnythserver.bookmark.application.exception.IdempotencyConflictException;
import com.onnyth.onnythserver.bookmark.application.port.BookmarkRepository;
import com.onnyth.onnythserver.bookmark.application.port.out.BookmarkEventPublisher;
import com.onnyth.onnythserver.bookmark.domain.event.BookmarkCreated;
import com.onnyth.onnythserver.bookmark.domain.model.Bookmark;
import com.onnyth.onnythserver.shared.idempotency.application.IdempotencyResponse;
import com.onnyth.onnythserver.shared.idempotency.application.IdempotencySerializer;
import com.onnyth.onnythserver.shared.idempotency.application.IdempotencyService;
import com.onnyth.onnythserver.shared.utils.RequestHasher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookmarkUseCaseService {

    private final BookmarkRepository bookmarkRepository;
    private final IdempotencyService idempotencyService;
    private final IdempotencySerializer idempotencySerializer;
    private final RequestHasher requestHasher;
    private final BookmarkEventPublisher bookmarkEventPublisher;

    /**
     * Creates a bookmark. If {@code idempotencyKey} matches a previously cached request with
     * the same content, returns the cached response instead of creating a new bookmark; if it
     * matches a cached request with different content, throws {@link IdempotencyConflictException}.
     * On successful creation, publishes a {@link BookmarkCreated} event.
     */
    @Transactional
    public CreateBookmarkResponse createBookmark(CreateBookmarkCommand command, String idempotencyKey) {
        String requestHash = requestHasher.hash(canonicalize(command));

        Optional<IdempotencyResponse> idempotencyResponse = safeGetIdempotencyRecord(idempotencyKey);
        if (idempotencyResponse.isPresent()) {
            log.info("Idempotent request detected for key {}", idempotencyKey);

            IdempotencyResponse idempotencyResponseRecord = idempotencyResponse.get();

            if (!requestHash.equals(idempotencyResponseRecord.requestHash())) {
                throw new IdempotencyConflictException();
            }

            return idempotencySerializer.deserialize(idempotencyResponse.get().requestBody(), CreateBookmarkResponse.class);
        }

        Set<String> tags = command.tags() == null ? Set.of() : command.tags();

        Bookmark bookmark = Bookmark.builder()
                .url(command.url())
                .title(command.title())
                .tags(tags)
                .build();

        Bookmark savedBookmark = bookmarkRepository.save(bookmark);

        publishBookmarkCreated(savedBookmark);

        CreateBookmarkResponse createBookmarkResponse = CreateBookmarkResponse.fromDomain(savedBookmark);

        safeSaveIdempotencyRecord(
                idempotencyKey,
                idempotencySerializer.serialize(
                        requestHash,
                        createBookmarkResponse
                )
        );

        log.info("Created bookmark {} for url {}", bookmark.getId(), bookmark.getUrl());
        return createBookmarkResponse;
    }

    /**
     * Builds a canonical string representation of the command for hashing, ignoring the
     * iteration order of the {@code tags} set.
     */
    private String canonicalize(CreateBookmarkCommand command) {
        Set<String> tags = command.tags() == null ? Set.of() : command.tags();
        String sortedTags = tags.stream().sorted().collect(Collectors.joining(","));
        return command.url() + "|" + command.title() + "|" + sortedTags;
    }

    /**
     * Reads the cached idempotency record for the given key, returning an empty result if
     * the read fails.
     */
    private Optional<IdempotencyResponse> safeGetIdempotencyRecord(String idempotencyKey) {
        try {
            return idempotencyService.get(idempotencyKey);
        } catch (Exception e) {
            log.warn("Idempotency cache read failed for key {}; treating as cache miss", idempotencyKey, e);
            return Optional.empty();
        }
    }

    /**
     * Persists the idempotency record for the given key, logging a warning if the write fails.
     */
    private void safeSaveIdempotencyRecord(String idempotencyKey, IdempotencyResponse response) {
        try {
            idempotencyService.save(idempotencyKey, response);
        } catch (Exception e) {
            log.warn("Idempotency cache write failed for key {}; bookmark was still created", idempotencyKey, e);
        }
    }

    /**
     * Builds a {@link BookmarkCreated} event for the given bookmark and publishes it after the
     * current transaction commits. If no transaction is active, publishes immediately.
     */
    private void publishBookmarkCreated(Bookmark bookmark) {
        BookmarkCreated event = BookmarkCreated.of(bookmark.getId(), bookmark.getTitle(), bookmark.getUrl());

        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    safePublish(event);
                }
            });
        } else {
            safePublish(event);
        }
    }

    /**
     * Publishes the given event via {@link BookmarkEventPublisher}, logging a warning instead
     * of throwing if publishing fails.
     */
    private void safePublish(BookmarkCreated event) {
        try {
            bookmarkEventPublisher.publish(event);
        } catch (Exception e) {
            log.warn("Failed to publish BookmarkCreated event for bookmark {}; bookmark was still created",
                    event.bookmarkId(), e);
        }
    }

    public Optional<Bookmark> findBookmarkById(UUID id) {
        return bookmarkRepository.findById(id);
    }

    public Page<Bookmark> findByTag(String tag, Pageable pageable) {
        if (tag == null || tag.isBlank()) {
            return bookmarkRepository.findAll(pageable);
        }
        return bookmarkRepository.findByTag(tag, pageable);
    }

    @Transactional
    public Bookmark updateBookmark(UUID id, UpdateBookmarkCommand command) {
        Set<String> tags = command.tags() == null ? Set.of() : command.tags();

        Bookmark existing = bookmarkRepository.findById(id)
                .orElseThrow(() -> new BookmarkNotFoundException(id.toString()));

        Bookmark updated = existing.toBuilder()
                .url(command.url())
                .title(command.title())
                .tags(tags)
                .build();

        Bookmark saved = bookmarkRepository.save(updated);
        log.info("Updated bookmark {} for url {}", saved.getId(), saved.getUrl());
        return saved;
    }

    @Transactional
    public void deleteBookmark(UUID id) {
        Bookmark bookmark = bookmarkRepository.findById(id)
                .orElseThrow(() -> new BookmarkNotFoundException(id.toString()));

        bookmarkRepository.delete(bookmark);
        log.info("Deleted bookmark {} for url {}", bookmark.getId(), bookmark.getUrl());
    }
}

