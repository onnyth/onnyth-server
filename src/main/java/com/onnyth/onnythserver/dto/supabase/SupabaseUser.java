package com.onnyth.onnythserver.dto.supabase;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record SupabaseUser(
        @JsonProperty("id") String id,
        @JsonProperty("email") String email
) {
}
