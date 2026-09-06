package com.onnyth.onnythserver.lifestats.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

/**
 * Tracks a user's education history. Child table of the Wisdom domain.
 * A user can have multiple education entries (degrees, certifications).
 * Only one can be marked as is_highest (enforced by partial unique index).
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserEducation {

    private UUID id;
    private UUID userId;
    private EducationLevel level;
    private String institution;
    private String fieldOfStudy;
    private Integer graduationYear;

    @Builder.Default
    private Boolean isHighest = false;

    @Builder.Default
    private Instant createdAt = Instant.now();
}
