package com.onnyth.onnythserver.bookmark.application.usecase;

import com.onnyth.onnythserver.bookmark.application.command.CreateBookmarkCommand;
import com.onnyth.onnythserver.bookmark.application.command.UpdateBookmarkCommand;
import com.onnyth.onnythserver.bookmark.application.exception.BookmarkNotFoundException;
import com.onnyth.onnythserver.bookmark.application.port.BookmarkRepository;
import com.onnyth.onnythserver.bookmark.domain.model.Bookmark;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookmarkUseCaseService {

    private final BookmarkRepository bookmarkRepository;

    @Transactional
    public Bookmark createBookmark(CreateBookmarkCommand command) {
        Set<String> tags = command.tags() == null ? Set.of() : command.tags();

        Bookmark bookmark = Bookmark.builder()
                .url(command.url())
                .title(command.title())
                .tags(tags)
                .build();

        Bookmark saved = bookmarkRepository.save(bookmark);
        log.info("Created bookmark {} for url {}", saved.getId(), saved.getUrl());
        return saved;
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

