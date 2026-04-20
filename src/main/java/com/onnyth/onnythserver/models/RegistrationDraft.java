package com.onnyth.onnythserver.models;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Stores in-progress multi-form registration data as a JSONB blob.
 * Each user has at most one draft (1:1 with users table).
 * Drafts are deleted on successful registration commit.
 */
@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "registration_drafts")
public class RegistrationDraft {

    @Id
    @Column(name = "user_id", nullable = false, updatable = false)
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "current_step", nullable = false, length = 30)
    @Builder.Default
    private RegistrationStep currentStep = RegistrationStep.PHONE;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "draft_data", nullable = false, columnDefinition = "jsonb")
    @Builder.Default
    private Map<String, Object> draftData = new HashMap<>();

    @Version
    @Column(name = "version", nullable = false)
    @Builder.Default
    private Integer version = 1;

    @Column(name = "created_at", nullable = false, updatable = false)
    @ColumnDefault("now()")
    @Builder.Default
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    @ColumnDefault("now()")
    @Builder.Default
    private Instant updatedAt = Instant.now();

    @Column(name = "expires_at", nullable = false)
    @Builder.Default
    private Instant expiresAt = Instant.now().plusSeconds(30L * 24 * 60 * 60); // 30 days

    /**
     * Merges step data into the draft JSONB under the step key.
     */
    public void mergeStepData(RegistrationStep step, Map<String, Object> stepData) {
        this.draftData.put(step.name(), stepData);
        this.updatedAt = Instant.now();
    }

    /**
     * Gets the data for a specific step, or null if not yet saved.
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> getStepData(RegistrationStep step) {
        Object data = this.draftData.get(step.name());
        if (data instanceof Map) {
            return (Map<String, Object>) data;
        }
        return null;
    }

    /**
     * Checks whether a specific step has data saved.
     */
    public boolean hasStepData(RegistrationStep step) {
        return this.draftData.containsKey(step.name());
    }
}
