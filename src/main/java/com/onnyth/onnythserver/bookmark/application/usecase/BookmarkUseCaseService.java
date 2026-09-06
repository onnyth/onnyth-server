package com.onnyth.onnythserver.bookmark.application.usecase;

import com.onnyth.onnythserver.bookmark.adapter.in.rest.dto.CreateBookmarkResponse;
import com.onnyth.onnythserver.bookmark.application.command.CreateBookmarkCommand;
import com.onnyth.onnythserver.bookmark.application.command.UpdateBookmarkCommand;
import com.onnyth.onnythserver.bookmark.application.exception.BookmarkNotFoundException;
import com.onnyth.onnythserver.bookmark.application.exception.IdempotencyConflictException;
import com.onnyth.onnythserver.bookmark.application.port.BookmarkRepository;
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

    /**
     * Redis-backed idempotency is treated as a best-effort cache, not a system of record —
     * the same philosophy already accepted for Kafka publish failures (UC-1 A5: the bookmark
     * is source of truth, publish failures are logged for reconciliation). A transient Redis
     * failure on read or write must never roll back or block a successful bookmark creation.
     * Accepted tradeoff: if Redis is unavailable during the idempotency window, a client retry
     * with the same Idempotency-Key will not find the cached response and may create a
     * duplicate bookmark. This is deliberate until a stronger dedup mechanism (e.g. a DB-level
     * unique constraint on the request hash, or the Phase 3 Outbox) is introduced.
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
     * Builds a canonical string representation of the command for hashing, independent of
     * the iteration order of the {@code tags} set, so logically identical retries always
     * produce the same idempotency hash.
     */
    private String canonicalize(CreateBookmarkCommand command) {
        Set<String> tags = command.tags() == null ? Set.of() : command.tags();
        String sortedTags = tags.stream().sorted().collect(Collectors.joining(","));
        return command.url() + "|" + command.title() + "|" + sortedTags;
    }

    /**
     * Reads the cached idempotency record, treating a Redis failure as a cache miss so the
     * request can still proceed to create the bookmark rather than fail outright.
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
     * Persists the idempotency record for future replay, tolerating Redis failures so a
     * transient cache outage never rolls back an already-persisted bookmark.
     */
    private void safeSaveIdempotencyRecord(String idempotencyKey, IdempotencyResponse response) {
        try {
            idempotencyService.save(idempotencyKey, response);
        } catch (Exception e) {
            log.warn("Idempotency cache write failed for key {}; bookmark was still created", idempotencyKey, e);
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

