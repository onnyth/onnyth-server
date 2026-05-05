package com.onnyth.onnythserver.repository;

import com.onnyth.onnythserver.models.ProfileVote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository for profile votes.
 * Composite PK is (voterId, targetId).
 */
@Repository
public interface ProfileVoteRepository extends JpaRepository<ProfileVote, String> {

    Optional<ProfileVote> findByVoterIdAndTargetId(UUID voterId, UUID targetId);

    long countByTargetIdAndIsUpvoteTrue(UUID targetId);

    long countByTargetIdAndIsUpvoteFalse(UUID targetId);

    void deleteByVoterIdAndTargetId(UUID voterId, UUID targetId);

    boolean existsByVoterIdAndTargetId(UUID voterId, UUID targetId);

    /** Net vote score: upvotes - downvotes for a given target. */
    @Query("""
            SELECT COALESCE(SUM(CASE WHEN v.isUpvote = true THEN 1 ELSE -1 END), 0)
            FROM ProfileVote v WHERE v.targetId = :targetId
            """)
    int computeNetVoteScore(@Param("targetId") UUID targetId);
}
