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
 * Tracks a user's knowledge and intellectual profile for the Wisdom domain.
 * Education entries and X-Factors are stored as separate child tables.
 * Hobbies are stored as a JSONB array for simplicity.
 */
@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "user_wisdom", uniqueConstraints = @UniqueConstraint(columnNames = "user_id"))
public class UserWisdom {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "user_id", nullable = false, unique = true)
    private UUID userId;

    /**
     * Predefined habit IDs selected by the user (e.g., "READING", "EXERCISE", "MEDITATION").
     * Max 5 selections. Stored as JSONB array of string keys.
     * Previously named 'hobbies' — column name retained for DB compatibility.
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "hobbies", nullable = false, columnDefinition = "jsonb")
    @Builder.Default
    private List<String> habitIds = new ArrayList<>();

    /**
     * ISO 639-1 language codes spoken by the user (e.g., ["en", "fr", "ar"]).
     * Max 5 selections.
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "languages", nullable = false, columnDefinition = "jsonb")
    @Builder.Default
    private List<String> languages = new ArrayList<>();

    /** Standardized education level key (e.g., BACHELORS, MASTERS, PHD). */
    @Column(name = "education_level", length = 30)
    private String educationLevel;

    /** Name of the institution (from Hipo dataset, or manually typed as fallback). */
    @Column(name = "institution_name", length = 200)
    private String institutionName;

    /** Year of graduation or expected graduation. */
    @Column(name = "graduation_year")
    private Integer graduationYear;

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
