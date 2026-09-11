package com.onnyth.onnythserver.bookmark.application.port.out;

import com.onnyth.onnythserver.bookmark.domain.event.BookmarkCreated;

/**
 * Publishes bookmark domain events.
 */
public interface BookmarkEventPublisher {

    /**
     * Publishes the given event.
     */
    void publish(BookmarkCreated event);
}
