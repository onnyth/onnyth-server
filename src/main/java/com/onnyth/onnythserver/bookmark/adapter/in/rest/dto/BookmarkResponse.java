package com.onnyth.onnythserver.bookmark.adapter.in.rest.dto;

import com.onnyth.onnythserver.bookmark.domain.model.Bookmark;
import lombok.Builder;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

@Builder
public record BookmarkResponse(
        UUID id,
        String url,
        String title,
        Set<String> tags,
        Instant createdAt,
        Instant updatedAt
) {
    public static BookmarkResponse fromDomain(Bookmark bookmark) {
        return BookmarkResponse.builder()
                .id(bookmark.getId())
                .url(bookmark.getUrl())
                .title(bookmark.getTitle())
                .tags(bookmark.getTags())
                .createdAt(bookmark.getCreatedAt())
                .updatedAt(bookmark.getUpdatedAt())
                .build();
    }
}

