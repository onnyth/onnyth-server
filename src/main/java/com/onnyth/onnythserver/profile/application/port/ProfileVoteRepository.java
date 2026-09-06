package com.onnyth.onnythserver.profile.application.port;

import com.onnyth.onnythserver.profile.domain.model.ProfileVote;

import java.util.Optional;
import java.util.UUID;

/**
 * Port for persisting and querying profile votes.
 */
public interface ProfileVoteRepository {

    ProfileVote save(ProfileVote vote);

    Optional<ProfileVote> findByVoterIdAndTargetId(UUID voterId, UUID targetId);

    long countByTargetIdAndIsUpvoteTrue(UUID targetId);

    long countByTargetIdAndIsUpvoteFalse(UUID targetId);

    void deleteByVoterIdAndTargetId(UUID voterId, UUID targetId);

    boolean existsByVoterIdAndTargetId(UUID voterId, UUID targetId);

    /** Net vote score: upvotes - downvotes for a given target. */
    int computeNetVoteScore(UUID targetId);

    void deleteAllByVoterId(UUID voterId);

    void deleteAllByTargetId(UUID targetId);
}
