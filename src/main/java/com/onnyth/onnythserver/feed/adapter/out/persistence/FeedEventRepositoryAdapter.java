package com.onnyth.onnythserver.feed.adapter.out.persistence;

import com.onnyth.onnythserver.feed.application.port.FeedEventRepository;
import com.onnyth.onnythserver.feed.domain.model.FeedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class FeedEventRepositoryAdapter implements FeedEventRepository {

    private final FeedEventJpaRepository feedEventJpaRepository;

    @Override
    public FeedEvent save(FeedEvent feedEvent) {
        FeedEventEntity saved = feedEventJpaRepository.save(FeedEventPersistenceMapper.toEntity(feedEvent));
        return FeedEventPersistenceMapper.toDomain(saved);
    }

    @Override
    public Page<FeedEvent> findFriendFeed(UUID userId, Pageable pageable) {
        return feedEventJpaRepository.findFriendFeed(userId, pageable)
                .map(FeedEventPersistenceMapper::toDomain);
    }

    @Override
    public void deleteAllByUserId(UUID userId) {
        feedEventJpaRepository.deleteAllByUserId(userId);
    }
}
