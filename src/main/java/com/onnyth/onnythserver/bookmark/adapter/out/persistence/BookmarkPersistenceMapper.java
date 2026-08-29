package com.onnyth.onnythserver.bookmark.adapter.out.persistence;

import com.onnyth.onnythserver.bookmark.domain.model.Bookmark;

import java.util.Set;

public final class BookmarkPersistenceMapper {

    private BookmarkPersistenceMapper() {
    }

    public static Bookmark toDomain(BookmarkEntity entity) {
        return Bookmark.builder()
                .id(entity.getId())
                .url(entity.getUrl())
                .title(entity.getTitle())
                .tags(entity.getTags() == null ? Set.of() : entity.getTags())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public static BookmarkEntity toEntity(Bookmark domain) {
        return BookmarkEntity.builder()
                .id(domain.getId())
                .url(domain.getUrl())
                .title(domain.getTitle())
                .tags(domain.getTags() == null ? Set.of() : domain.getTags())
                .createdAt(domain.getCreatedAt())
                .updatedAt(domain.getUpdatedAt())
                .build();
    }
}

