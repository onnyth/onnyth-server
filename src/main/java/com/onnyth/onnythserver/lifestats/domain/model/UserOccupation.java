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
 * Tracks a user's occupation/career information.
 * One "current" occupation per user (enforced by partial unique index).
 * Supports multiple historical occupations for future expansion.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserOccupation {

    private UUID id;
    private UUID userId;
    private String jobTitle;
    private String rawJobTitle;
    private String companyName;
    private String rawCompanyName;

    @Builder.Default
    private Boolean isVerified = false;

    private String industry;
    private EmploymentType employmentType;
    private Integer yearsExperience;

    @Builder.Default
    private List<String> skills = new ArrayList<>();

    @Builder.Default
    private Boolean isCurrent = true;

    @Builder.Default
    private Integer score = 0;

    @Builder.Default
    private Instant createdAt = Instant.now();

    @Builder.Default
    private Instant updatedAt = Instant.now();
}
