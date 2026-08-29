package com.onnyth.onnythserver.bookmark.adapter.out.persistence;

import com.onnyth.onnythserver.bookmark.application.port.BookmarkRepository;
import com.onnyth.onnythserver.bookmark.domain.model.Bookmark;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class BookmarkRepositoryAdapter implements BookmarkRepository {

    private final BookmarkJpaRepository bookmarkJpaRepository;

    @Override
    public Bookmark save(Bookmark bookmark) {
        BookmarkEntity saved = bookmarkJpaRepository.save(BookmarkPersistenceMapper.toEntity(bookmark));
        return BookmarkPersistenceMapper.toDomain(saved);
    }

    @Override
    public Optional<Bookmark> findById(UUID id) {
        return bookmarkJpaRepository.findById(id).map(BookmarkPersistenceMapper::toDomain);
    }

    @Override
    public Page<Bookmark> findAll(Pageable pageable) {
        return bookmarkJpaRepository.findAll(pageable).map(BookmarkPersistenceMapper::toDomain);
    }

    @Override
    public Page<Bookmark> findByTag(String tag, Pageable pageable) {
        return bookmarkJpaRepository.findByTagsContaining(tag, pageable).map(BookmarkPersistenceMapper::toDomain);
    }

    @Override
    public void delete(Bookmark bookmark) {
        bookmarkJpaRepository.delete(BookmarkPersistenceMapper.toEntity(bookmark));
    }
}

