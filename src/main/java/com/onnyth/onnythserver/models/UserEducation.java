package com.onnyth.onnythserver.models;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;

import java.time.Instant;
import java.util.UUID;

/**
 * Tracks a user's education history. Child table of the Wisdom domain.
 * A user can have multiple education entries (degrees, certifications).
 * Only one can be marked as is_highest (enforced by partial unique index).
 */
@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "user_education")
public class UserEducation {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "level", nullable = false, length = 30)
    private EducationLevel level;

    @Column(name = "institution", nullable = false, length = 200)
    private String institution;

    @Column(name = "field_of_study", length = 100)
    private String fieldOfStudy;

    @Column(name = "graduation_year")
    private Integer graduationYear;

    @Column(name = "is_highest", nullable = false)
    @Builder.Default
    private Boolean isHighest = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    @ColumnDefault("now()")
    @Builder.Default
    private Instant createdAt = Instant.now();
}
