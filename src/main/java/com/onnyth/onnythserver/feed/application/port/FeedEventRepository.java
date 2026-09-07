package com.onnyth.onnythserver.feed.application.port;

import com.onnyth.onnythserver.feed.domain.model.FeedEvent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface FeedEventRepository {

    FeedEvent save(FeedEvent feedEvent);

    Page<FeedEvent> findFriendFeed(UUID userId, Pageable pageable);

    void deleteAllByUserId(UUID userId);
}
