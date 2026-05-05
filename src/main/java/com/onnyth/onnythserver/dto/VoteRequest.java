package com.onnyth.onnythserver.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

/**
 * Request body for casting or updating a profile vote.
 */
public record VoteRequest(
        @NotNull UUID targetUserId,
        @NotNull Boolean isUpvote
) {}
