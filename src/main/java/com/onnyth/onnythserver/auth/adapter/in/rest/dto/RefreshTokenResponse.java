package com.onnyth.onnythserver.auth.adapter.in.rest.dto;

public record RefreshTokenResponse(
        String accessToken,
        String refreshToken,
        long expiresAt
) {}
