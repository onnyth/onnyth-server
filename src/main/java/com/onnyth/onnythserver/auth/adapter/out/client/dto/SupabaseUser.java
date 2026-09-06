package com.onnyth.onnythserver.auth.adapter.out.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record SupabaseUser(
        @JsonProperty("id") String id,
        @JsonProperty("email") String email
) {
}
