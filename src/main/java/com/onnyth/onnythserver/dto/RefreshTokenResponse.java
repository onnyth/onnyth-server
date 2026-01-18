package com.onnyth.onnythserver.dto;

public record RefreshTokenResponse(
        String accessToken,
        String refreshToken,
        int expiresIn,
        String tokenType
) {}
