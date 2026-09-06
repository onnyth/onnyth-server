package com.onnyth.onnythserver.auth.adapter.in.rest.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record RefreshTokenRequest(
        @JsonProperty("refresh_token")
        String refreshToken
) {}
