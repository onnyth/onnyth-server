package com.onnyth.onnythserver.registration.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Stores in-progress multi-form registration data as a JSON blob.
 * Each user has at most one draft (1:1 with users table).
 * Drafts are deleted on successful registration commit.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegistrationDraft {

    private UUID userId;

    @Builder.Default
    private RegistrationStep currentStep = RegistrationStep.PHONE;

    @Builder.Default
    private Map<String, Object> draftData = new HashMap<>();

    @Builder.Default
    private Integer version = 1;

    @Builder.Default
    private Instant createdAt = Instant.now();

    @Builder.Default
    private Instant updatedAt = Instant.now();

    @Builder.Default
    private Instant expiresAt = Instant.now().plusSeconds(30L * 24 * 60 * 60);

    public void mergeStepData(RegistrationStep step, Map<String, Object> stepData) {
        this.draftData.put(step.name(), stepData);
        this.updatedAt = Instant.now();
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> getStepData(RegistrationStep step) {
        Object data = this.draftData.get(step.name());
        if (data instanceof Map) {
            return (Map<String, Object>) data;
        }
        return null;
    }

    public boolean hasStepData(RegistrationStep step) {
        return this.draftData.containsKey(step.name());
    }
}
