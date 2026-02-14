package com.onnyth.onnythserver.dto;

public record SupabaseSession(
        String access_token,
        String refresh_token,
        String token_type,
        Integer expires_in
) {}
