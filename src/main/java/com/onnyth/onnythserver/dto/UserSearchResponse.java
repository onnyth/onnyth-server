package com.onnyth.onnythserver.dto;

import lombok.Builder;

import java.util.UUID;

@Builder
public record UserSearchResponse(
        UUID userId,
        String username,
        String fullName,
        String profilePic,
        String rankTier,
        boolean isFriend,
        boolean requestPending) {
}
