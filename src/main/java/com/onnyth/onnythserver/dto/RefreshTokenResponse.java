package com.onnyth.onnythserver.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record RefreshTokenResponse(
        String accessToken,
        String refreshToken,
        int expiresIn,
        String tokenType
) {}
