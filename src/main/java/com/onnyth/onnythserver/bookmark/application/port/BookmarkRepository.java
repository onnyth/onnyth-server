package com.onnyth.onnythserver.bookmark.application.port;

import com.onnyth.onnythserver.bookmark.domain.model.Bookmark;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

public interface BookmarkRepository {

    Bookmark save(Bookmark bookmark);

    Optional<Bookmark> findById(UUID id);

    Page<Bookmark> findAll(Pageable pageable);

    Page<Bookmark> findByTag(String tag, Pageable pageable);

    void delete(Bookmark bookmark);
}

