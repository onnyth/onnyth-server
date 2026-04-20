package com.onnyth.onnythserver.repository;

import com.onnyth.onnythserver.models.FeedEvent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface FeedEventRepository extends JpaRepository<FeedEvent, UUID> {

    /**
     * Get feed events from the user's friends, ordered by most recent.
     * Joins with the friendships table to get only friend events.
     */
    @Query("SELECT fe FROM FeedEvent fe WHERE fe.userId IN " +
           "(SELECT f.friendId FROM Friendship f WHERE f.userId = :userId) " +
           "ORDER BY fe.createdAt DESC")
    Page<FeedEvent> findFriendFeed(@Param("userId") UUID userId, Pageable pageable);
}
