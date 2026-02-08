package com.onnyth.onnythserver.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.onnyth.onnythserver.dto.supabase.SupabaseUser;

public record LoginResponse (
        String accessToken,
        String refreshToken,
        int expiresIn,
        String tokenType,
        long expiresAt,
        SupabaseUser supabaseUser
){ }
