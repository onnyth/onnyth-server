package com.onnyth.onnythserver.bookmark.adapter.out.kafka;

import com.onnyth.onnythserver.bookmark.application.port.out.BookmarkEventPublisher;
import com.onnyth.onnythserver.bookmark.domain.event.BookmarkCreated;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * Publishes {@link BookmarkCreated} events to {@link BookmarkKafkaTopics#BOOKMARK_CREATED_V1},
 * keyed by bookmarkId. Publish failures are caught and logged rather than propagated.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class KafkaBookmarkEventPublisher implements BookmarkEventPublisher {

    private final KafkaTemplate<String, BookmarkCreated> kafkaTemplate;

    /**
     * Sends the event to the bookmark-created topic. Logs a warning on failure instead of
     * throwing.
     */
    @Override
    public void publish(BookmarkCreated event) {
        try {
            kafkaTemplate.send(BookmarkKafkaTopics.BOOKMARK_CREATED_V1, event.bookmarkId().toString(), event)
                    .whenComplete((result, ex) -> {
                        if (ex != null) {
                            log.warn("Failed to publish BookmarkCreated event {} for bookmark {}",
                                    event.eventId(), event.bookmarkId(), ex);
                        } else {
                            log.debug("Published BookmarkCreated event {} for bookmark {}",
                                    event.eventId(), event.bookmarkId());
                        }
                    });
        } catch (Exception e) {
            log.warn("Failed to submit BookmarkCreated event {} for bookmark {}",
                    event.eventId(), event.bookmarkId(), e);
        }
    }
}
