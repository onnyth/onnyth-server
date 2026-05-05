package com.onnyth.onnythserver.service;

import com.onnyth.onnythserver.dto.VoteResponse;
import com.onnyth.onnythserver.exceptions.UserNotFoundException;
import com.onnyth.onnythserver.models.ProfileVote;
import com.onnyth.onnythserver.models.User;
import com.onnyth.onnythserver.repository.ProfileVoteRepository;
import com.onnyth.onnythserver.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

/**
 * Manages upvote / downvote interactions on user profiles.
 *
 * Rules:
 *  - A user cannot vote on their own profile (400).
 *  - Each voter can have at most one vote per target (upsert behaviour).
 *  - user.voteScore is kept denormalised for fast reads: recomputed on every change.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ProfileVoteService {

    private final ProfileVoteRepository voteRepository;
    private final UserRepository userRepository;

    /**
     * Cast or update a vote. If the same direction is already cast, this is a no-op.
     * If the opposite direction is cast, the vote is flipped.
     *
     * @param voterId  the user casting the vote
     * @param targetId the profile being voted on
     * @param isUpvote true = upvote, false = downvote
     * @return updated VoteResponse
     */
    @Transactional
    public VoteResponse castVote(UUID voterId, UUID targetId, boolean isUpvote) {
        if (voterId.equals(targetId)) {
            throw new IllegalArgumentException("Users cannot vote on their own profile");
        }

        // Ensure target exists
        User target = userRepository.findById(targetId)
                .orElseThrow(() -> new UserNotFoundException(targetId.toString()));

        Optional<ProfileVote> existing = voteRepository.findByVoterIdAndTargetId(voterId, targetId);
        if (existing.isPresent()) {
            ProfileVote vote = existing.get();
            if (vote.getIsUpvote() == isUpvote) {
                // Same direction — idempotent, just return current state
                return buildResponse(target, voterId, targetId);
            }
            vote.setIsUpvote(isUpvote);
            vote.setVotedAt(Instant.now());
            voteRepository.save(vote);
        } else {
            voteRepository.save(ProfileVote.builder()
                    .voterId(voterId)
                    .targetId(targetId)
                    .isUpvote(isUpvote)
                    .build());
        }

        // Recompute net score and persist on user
        int netScore = voteRepository.computeNetVoteScore(targetId);
        target.setVoteScore(netScore);
        userRepository.save(target);

        log.info("Vote cast: voter={} target={} isUpvote={} newScore={}", voterId, targetId, isUpvote, netScore);
        return buildResponse(target, voterId, targetId);
    }

    /**
     * Remove the caller's vote on a target profile.
     * No-op if no vote exists.
     *
     * @return updated VoteResponse with myVote=null
     */
    @Transactional
    public VoteResponse removeVote(UUID voterId, UUID targetId) {
        User target = userRepository.findById(targetId)
                .orElseThrow(() -> new UserNotFoundException(targetId.toString()));

        if (voteRepository.existsByVoterIdAndTargetId(voterId, targetId)) {
            voteRepository.deleteByVoterIdAndTargetId(voterId, targetId);
            int netScore = voteRepository.computeNetVoteScore(targetId);
            target.setVoteScore(netScore);
            userRepository.save(target);
            log.info("Vote removed: voter={} target={} newScore={}", voterId, targetId, netScore);
        }

        return VoteResponse.builder()
                .targetUserId(targetId)
                .voteScore(target.getVoteScore() != null ? target.getVoteScore() : 0)
                .myVote(null)
                .build();
    }

    /**
     * Get the current caller's vote on a target user's profile.
     *
     * @return true=upvote, false=downvote, null=no vote
     */
    @Transactional(readOnly = true)
    public VoteResponse getMyVote(UUID voterId, UUID targetId) {
        User target = userRepository.findById(targetId)
                .orElseThrow(() -> new UserNotFoundException(targetId.toString()));
        return buildResponse(target, voterId, targetId);
    }

    // ─── Private helpers ─────────────────────────────────────────────────────

    private VoteResponse buildResponse(User target, UUID voterId, UUID targetId) {
        Optional<ProfileVote> myVote = voteRepository.findByVoterIdAndTargetId(voterId, targetId);
        return VoteResponse.builder()
                .targetUserId(targetId)
                .voteScore(target.getVoteScore() != null ? target.getVoteScore() : 0)
                .myVote(myVote.map(ProfileVote::getIsUpvote).orElse(null))
                .build();
    }
}
