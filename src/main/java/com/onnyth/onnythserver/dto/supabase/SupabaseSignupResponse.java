package com.onnyth.onnythserver.dto.supabase;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;

@JsonIgnoreProperties(ignoreUnknown = true)
public record SupabaseSignupResponse(
        @JsonProperty("id") String id,
        @JsonProperty("email") String email,
        @JsonProperty("confirmation_sent_at") String confirmationSentAt
) {}
