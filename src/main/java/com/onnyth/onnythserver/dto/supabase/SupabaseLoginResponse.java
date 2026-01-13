package com.onnyth.onnythserver.dto.supabase;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record SupabaseLoginResponse(

        @JsonProperty("access_token")
        String accessToken,

        @JsonProperty("refresh_token")
        String refreshToken,

        @JsonProperty("expires_in")
        int expiresIn,

        @JsonProperty("token_type")
        String tokenType,

        @JsonProperty("expires_at")
        long expiresAt,

        @JsonProperty("user")
        SupabaseUser supabaseUser
) {}
