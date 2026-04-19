package com.onnyth.onnythserver.models;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Defines the ordered steps in the multi-form registration flow.
 * To add a new step: add an enum value here, create a step validator,
 * and add a persister method in RegistrationCommitService.
 */
public enum RegistrationStep {

    PHONE(0, false),
    NAME(1, false),
    IMAGE(2, true),
    OCCUPATION(3, true),
    WEALTH(4, true),
    PHYSIQUE(5, true),
    WISDOM(6, true),
    CHARISMA(7, true);

    private final int order;
    private final boolean optional;

    RegistrationStep(int order, boolean optional) {
        this.order = order;
        this.optional = optional;
    }

    public int getOrder() {
        return order;
    }

    public boolean isOptional() {
        return optional;
    }

    public boolean isRequired() {
        return !optional;
    }

    /**
     * Returns the next step in the flow, or null if this is the last step.
     */
    public RegistrationStep next() {
        RegistrationStep[] steps = values();
        int nextOrdinal = this.ordinal() + 1;
        return nextOrdinal < steps.length ? steps[nextOrdinal] : null;
    }

    /**
     * Returns all steps in order.
     */
    public static List<RegistrationStep> allInOrder() {
        return Arrays.stream(values())
                .sorted((a, b) -> Integer.compare(a.order, b.order))
                .collect(Collectors.toList());
    }

    /**
     * Returns all required steps.
     */
    public static List<RegistrationStep> requiredSteps() {
        return Arrays.stream(values())
                .filter(RegistrationStep::isRequired)
                .collect(Collectors.toList());
    }

    /**
     * Safely parse a step key string to enum, returning null if invalid.
     */
    public static RegistrationStep fromKey(String key) {
        try {
            return valueOf(key.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
