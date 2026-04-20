package com.onnyth.onnythserver.repository;

import com.onnyth.onnythserver.models.Follow;
import com.onnyth.onnythserver.models.FollowId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface FollowRepository extends JpaRepository<Follow, FollowId> {

    /**
     * Count how many users are following a given user (Onnyth followers count).
     */
    @Query("SELECT COUNT(f) FROM Follow f WHERE f.following.id = :userId")
    long countByFollowingId(UUID userId);

    /**
     * Count how many users a given user is following.
     */
    @Query("SELECT COUNT(f) FROM Follow f WHERE f.follower.id = :userId")
    long countByFollowerId(UUID userId);

    boolean existsById(FollowId id);
}
