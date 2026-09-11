package com.onnyth.onnythserver.bookmark.domain.event;

import java.time.Instant;
import java.util.UUID;

/**
 * Event published when a bookmark is created.
 */
public record BookmarkCreated(
        UUID eventId,
        String eventType,
        Instant occurredAt,
        UUID bookmarkId,
        String title,
        String url
) {

    public static final String EVENT_TYPE = "BookmarkCreated";

    public static BookmarkCreated of(UUID bookmarkId, String title, String url) {
        return new BookmarkCreated(UUID.randomUUID(), EVENT_TYPE, Instant.now(), bookmarkId, title, url);
    }
}