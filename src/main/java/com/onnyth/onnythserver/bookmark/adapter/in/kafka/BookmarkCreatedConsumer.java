package com.onnyth.onnythserver.bookmark.adapter.in.kafka;

import com.onnyth.onnythserver.bookmark.adapter.out.kafka.BookmarkKafkaTopics;
import com.onnyth.onnythserver.bookmark.domain.event.BookmarkCreated;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Consumes {@link BookmarkCreated} events from {@link BookmarkKafkaTopics#BOOKMARK_CREATED_V1}.
 */
@Component
@Slf4j
public class BookmarkCreatedConsumer {

    /**
     * Handles a received BookmarkCreated event.
     */
    @KafkaListener(
            topics = BookmarkKafkaTopics.BOOKMARK_CREATED_V1,
            groupId = "onnyth-bookmark"
    )
    public void consume(BookmarkCreated event) {
        log.info("Received BookmarkCreated event {} for bookmark {}", event.eventId(), event.bookmarkId());
    }
}
