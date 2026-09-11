package com.onnyth.onnythserver.bookmark.adapter.out.kafka;

/**
 * Kafka topic names used for bookmark events.
 */
public final class BookmarkKafkaTopics {

    /** Topic for BookmarkCreated events, keyed by bookmarkId. */
    public static final String BOOKMARK_CREATED_V1 = "bookmark.created.v1";

    private BookmarkKafkaTopics() {
    }
}
