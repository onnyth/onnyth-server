package com.onnyth.onnythserver.lifestats.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

/**
 * Extraordinary achievements that make a user's profile stand out.
 * Child table of the Wisdom domain.
 * Examples: YouTube channel, company ownership, NGO work, publications, patents.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserXfactor {

    private UUID id;
    private UUID userId;
    private XfactorType type;
    private String title;
    private String description;
    private String evidenceUrl;
    private Integer metricValue;
    private String metricLabel;

    @Builder.Default
    private Boolean isVerified = false;

    @Builder.Default
    private Instant createdAt = Instant.now();
}
