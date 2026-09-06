package com.onnyth.onnythserver.profile.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data repository for profile votes.
 * Composite PK is (voterId, targetId).
 */
@Repository
public interface ProfileVoteJpaRepository extends JpaRepository<ProfileVoteEntity, String> {

    Optional<ProfileVoteEntity> findByVoterIdAndTargetId(UUID voterId, UUID targetId);

    long countByTargetIdAndIsUpvoteTrue(UUID targetId);

    long countByTargetIdAndIsUpvoteFalse(UUID targetId);

    void deleteByVoterIdAndTargetId(UUID voterId, UUID targetId);

    boolean existsByVoterIdAndTargetId(UUID voterId, UUID targetId);

    /** Net vote score: upvotes - downvotes for a given target. */
    @Query("""
            SELECT COALESCE(SUM(CASE WHEN v.isUpvote = true THEN 1 ELSE -1 END), 0)
            FROM ProfileVoteEntity v WHERE v.targetId = :targetId
            """)
    int computeNetVoteScore(@Param("targetId") UUID targetId);

    void deleteAllByVoterId(UUID voterId);

    void deleteAllByTargetId(UUID targetId);
}
