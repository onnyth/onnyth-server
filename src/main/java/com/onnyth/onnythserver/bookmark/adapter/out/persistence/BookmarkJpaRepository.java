package com.onnyth.onnythserver.bookmark.adapter.out.persistence;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface BookmarkJpaRepository extends JpaRepository<BookmarkEntity, UUID> {

    Page<BookmarkEntity> findByTagsContaining(String tag, Pageable pageable);
}

