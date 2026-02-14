package com.onnyth.onnythserver.dto;

public record RefreshTokenResponse(
        String accessToken,
        String refreshToken,
        long expiresAt
) {}
