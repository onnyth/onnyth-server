package com.onnyth.onnythserver.profile.adapter.in.rest.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

/**
 * Request body for casting or updating a profile vote.
 */
public record VoteRequest(
        @NotNull UUID targetUserId,
        @NotNull Boolean isUpvote
) {}
