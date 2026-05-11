package com.onnyth.onnythserver.models;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

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
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "user_occupation")
public class UserOccupation {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    /** Structured job title sourced from a dataset (e.g., O*NET). Null if user typed manually. */
    @Column(name = "job_title", length = 100)
    private String jobTitle;

    /** Raw job title typed by the user when not found in dataset. Used as fallback display value. */
    @Column(name = "raw_job_title", length = 150)
    private String rawJobTitle;

    /** Structured company name sourced from a dataset. Null if user typed manually. */
    @Column(name = "company_name", length = 150)
    private String companyName;

    /** Raw company name typed by the user when not found in dataset. Used as fallback display value. */
    @Column(name = "raw_company_name", length = 200)
    private String rawCompanyName;

    /**
     * True when both jobTitle and companyName were selected from a structured dataset.
     * Unverified entries receive a lower score multiplier in ScoreCalculationService.
     */
    @Column(name = "is_verified", nullable = false)
    @Builder.Default
    private Boolean isVerified = false;

    @Column(name = "industry", length = 50)
    private String industry;

    @Enumerated(EnumType.STRING)
    @Column(name = "employment_type", length = 20)
    private EmploymentType employmentType;

    @Column(name = "years_experience")
    private Integer yearsExperience;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "skills", nullable = false, columnDefinition = "jsonb")
    @Builder.Default
    private List<String> skills = new ArrayList<>();

    @Column(name = "is_current", nullable = false)
    @Builder.Default
    private Boolean isCurrent = true;

    @Column(name = "score", nullable = false)
    @Builder.Default
    private Integer score = 0;

    @Column(name = "created_at", nullable = false, updatable = false)
    @ColumnDefault("now()")
    @Builder.Default
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    @ColumnDefault("now()")
    @Builder.Default
    private Instant updatedAt = Instant.now();
}
