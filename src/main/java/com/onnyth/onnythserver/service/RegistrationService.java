package com.onnyth.onnythserver.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.onnyth.onnythserver.dto.registration.RegistrationStatusResponse;
import com.onnyth.onnythserver.dto.registration.RegistrationStepResponse;
import com.onnyth.onnythserver.exceptions.UsernameAlreadyExistsException;
import com.onnyth.onnythserver.models.RegistrationDraft;
import com.onnyth.onnythserver.models.RegistrationStep;
import com.onnyth.onnythserver.repository.RegistrationDraftRepository;
import com.onnyth.onnythserver.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Manages registration draft lifecycle: create/read/update drafts,
 * validate individual steps, and track step progression.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RegistrationService {

    private final RegistrationDraftRepository draftRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    /**
     * Get the current registration status for a user.
     * Returns empty status if no draft exists (user hasn't started registration).
     */
    public RegistrationStatusResponse getStatus(UUID userId) {
        Optional<RegistrationDraft> draftOpt = draftRepository.findById(userId);

        if (draftOpt.isEmpty()) {
            return RegistrationStatusResponse.empty();
        }

        RegistrationDraft draft = draftOpt.get();
        return new RegistrationStatusResponse(
                draft.getCurrentStep().name(),
                getCompletedSteps(draft),
                draft.getDraftData(),
                draft.getVersion()
        );
    }

    /**
     * Save data for a specific registration step.
     * Creates the draft if it doesn't exist, otherwise merges step data.
     */
    @Transactional
    public RegistrationStepResponse saveStep(UUID userId, RegistrationStep step, Map<String, Object> stepData) {
        // Step-specific validation
        validateStep(userId, step, stepData);

        RegistrationDraft draft = draftRepository.findById(userId)
                .orElseGet(() -> {
                    log.info("Creating new registration draft for user: {}", userId);
                    return RegistrationDraft.builder()
                            .userId(userId)
                            .currentStep(step)
                            .build();
                });

        // Merge the step data into the draft
        draft.mergeStepData(step, stepData);

        // Advance current step to the next uncompleted step
        RegistrationStep nextStep = step.next();
        if (nextStep != null) {
            draft.setCurrentStep(nextStep);
        } else {
            // Last step — keep current step as CHARISMA so client knows it's complete
            draft.setCurrentStep(step);
        }

        RegistrationDraft saved = draftRepository.save(draft);
        log.info("Saved registration step {} for user: {} (version: {})", step, userId, saved.getVersion());

        return new RegistrationStepResponse(
                saved.getCurrentStep().name(),
                getCompletedSteps(saved),
                saved.getVersion()
        );
    }

    /**
     * Save profile picture URL in the IMAGE step draft data.
     */
    @Transactional
    public RegistrationStepResponse saveImageStep(UUID userId, String profilePicUrl) {
        Map<String, Object> stepData = Map.of("profilePicUrl", profilePicUrl);
        return saveStep(userId, RegistrationStep.IMAGE, stepData);
    }

    /**
     * Get the draft for a user (used by commit service).
     */
    public Optional<RegistrationDraft> getDraft(UUID userId) {
        return draftRepository.findById(userId);
    }

    /**
     * Delete the draft after successful registration commit.
     */
    @Transactional
    public void deleteDraft(UUID userId) {
        draftRepository.deleteById(userId);
        log.info("Deleted registration draft for user: {}", userId);
    }

    /**
     * Get a list of completed step names from the draft.
     */
    private List<String> getCompletedSteps(RegistrationDraft draft) {
        return RegistrationStep.allInOrder().stream()
                .filter(draft::hasStepData)
                .map(Enum::name)
                .collect(Collectors.toList());
    }

    /**
     * Validate step-specific data. Throws exceptions on validation failure.
     */
    private void validateStep(UUID userId, RegistrationStep step, Map<String, Object> stepData) {
        switch (step) {
            case NAME -> validateNameStep(userId, stepData);
            case PHONE -> validatePhoneStep(stepData);
            default -> {
                // Optional steps have no strict validation at draft time
            }
        }
    }

    private void validateNameStep(UUID userId, Map<String, Object> stepData) {
        String username = (String) stepData.get("username");
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Username is required");
        }

        // Check username availability (excluding current user)
        if (userRepository.existsByUsernameIgnoreCase(username.trim())) {
            // Check if it's the same user's existing username
            var existingUser = userRepository.findById(userId).orElse(null);
            if (existingUser == null || !username.equalsIgnoreCase(existingUser.getUsername())) {
                throw new UsernameAlreadyExistsException(username);
            }
        }

        String displayName = (String) stepData.get("displayName");
        if (displayName == null || displayName.isBlank()) {
            throw new IllegalArgumentException("Display name is required");
        }
    }

    private void validatePhoneStep(Map<String, Object> stepData) {
        String phone = (String) stepData.get("phone");
        if (phone == null || phone.isBlank()) {
            throw new IllegalArgumentException("Phone number is required");
        }
    }
}
