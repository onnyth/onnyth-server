package com.onnyth.onnythserver.friendship.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.UUID;

public interface FollowJpaRepository extends JpaRepository<FollowEntity, FollowEntityId> {

    /**
     * Count how many users are following a given user (Onnyth followers count).
     */
    @Query("SELECT COUNT(f) FROM FollowEntity f WHERE f.following.id = :userId")
    long countByFollowingId(UUID userId);

    /**
     * Count how many users a given user is following.
     */
    @Query("SELECT COUNT(f) FROM FollowEntity f WHERE f.follower.id = :userId")
    long countByFollowerId(UUID userId);
}
