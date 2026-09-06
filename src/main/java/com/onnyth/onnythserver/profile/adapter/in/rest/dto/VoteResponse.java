package com.onnyth.onnythserver.profile.adapter.in.rest.dto;

import lombok.Builder;

import java.util.UUID;

/**
 * Response after casting, updating, or removing a profile vote.
 *
 * @param targetUserId the profile that was voted on
 * @param voteScore    updated net vote score (upvotes - downvotes)
 * @param myVote       the caller's current vote: true=up, false=down, null=no vote
 */
@Builder
public record VoteResponse(
        UUID targetUserId,
        int voteScore,
        Boolean myVote
) {}
