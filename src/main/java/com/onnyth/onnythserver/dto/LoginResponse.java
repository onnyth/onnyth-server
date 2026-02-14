package com.onnyth.onnythserver.dto;

import java.util.UUID;

public record LoginResponse(
        String accessToken,
        String refreshToken,
        long expiresAt,
        UserInfo user
) {
    public record UserInfo(
            UUID id,
            String email,
            String username,
            String fullName,
            String profilePic
    ) {}
}
