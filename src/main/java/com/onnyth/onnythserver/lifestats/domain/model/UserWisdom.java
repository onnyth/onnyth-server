package com.onnyth.onnythserver.lifestats.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Tracks a user's knowledge and intellectual profile for the Wisdom domain.
 * Education entries and X-Factors are stored as separate child tables.
 * Hobbies are stored as a JSONB array for simplicity.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserWisdom {

    private UUID id;
    private UUID userId;

    @Builder.Default
    private List<String> habitIds = new ArrayList<>();

    @Builder.Default
    private List<String> languages = new ArrayList<>();

    private String educationLevel;
    private String institutionName;
    private Integer graduationYear;

    @Builder.Default
    private Integer score = 0;

    @Builder.Default
    private Instant createdAt = Instant.now();

    @Builder.Default
    private Instant updatedAt = Instant.now();
}
