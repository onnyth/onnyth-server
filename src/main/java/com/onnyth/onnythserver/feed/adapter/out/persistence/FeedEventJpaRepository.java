package com.onnyth.onnythserver.feed.adapter.out.persistence;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface FeedEventJpaRepository extends JpaRepository<FeedEventEntity, UUID> {

    /**
     * Get feed events from the user's friends, ordered by most recent.
     * Joins with the friendships table to get only friend events.
     */
    @Query("SELECT fe FROM FeedEventEntity fe WHERE fe.userId IN " +
           "(SELECT f.friendId FROM FriendshipEntity f WHERE f.userId = :userId) " +
           "ORDER BY fe.createdAt DESC")
    Page<FeedEventEntity> findFriendFeed(@Param("userId") UUID userId, Pageable pageable);

    void deleteAllByUserId(UUID userId);
}
