package com.onnyth.onnythserver.service;

import com.onnyth.onnythserver.exceptions.IncompleteRegistrationException;
import com.onnyth.onnythserver.exceptions.UserNotFoundException;
import com.onnyth.onnythserver.models.*;
import com.onnyth.onnythserver.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Handles the atomic commit of registration data from draft JSONB
 * to normalized domain tables. All writes happen in a single transaction.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RegistrationCommitService {

    private final UserRepository userRepository;
    private final UserOccupationRepository userOccupationRepository;
    private final UserWealthRepository userWealthRepository;
    private final UserPhysiqueRepository userPhysiqueRepository;
    private final UserWisdomRepository userWisdomRepository;
    private final UserCharismaRepository userCharismaRepository;
    private final RegistrationService registrationService;

    /**
     * Atomically commits all draft data to normalized tables.
     * 1. Validates all required steps are present
     * 2. Persists data to each domain table
     * 3. Updates the users table (username, fullName, phone, profilePic, profileComplete)
     * 4. Deletes the draft
     */
    @Transactional
    public User commitRegistration(UUID userId) {
        RegistrationDraft draft = registrationService.getDraft(userId)
                .orElseThrow(() -> new IllegalStateException("No registration draft found for user: " + userId));

        // Validate all required steps are present
        List<String> missingSteps = RegistrationStep.requiredSteps().stream()
                .filter(step -> !draft.hasStepData(step))
                .map(Enum::name)
                .collect(Collectors.toList());

        if (!missingSteps.isEmpty()) {
            throw new IncompleteRegistrationException(missingSteps);
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId.toString()));

        Map<String, Object> draftData = draft.getDraftData();

        // Persist each step
        persistPhoneStep(user, draftData);
        persistNameStep(user, draftData);
        persistImageStep(user, draftData);
        persistOccupationStep(userId, draftData);
        persistWealthStep(userId, draftData);
        persistPhysiqueStep(userId, draftData);
        persistWisdomStep(userId, draftData);
        persistCharismaStep(userId, draftData);

        // Mark profile as complete
        user.setProfileComplete(true);
        user.setUpdatedAt(Instant.now());
        User savedUser = userRepository.save(user);

        // Delete the draft
        registrationService.deleteDraft(userId);

        log.info("Registration committed successfully for user: {} (username: {})",
                userId, savedUser.getUsername());

        return savedUser;
    }

    // ===================== Step Persisters =====================

    @SuppressWarnings("unchecked")
    private void persistPhoneStep(User user, Map<String, Object> draftData) {
        Map<String, Object> phoneData = getStepData(draftData, "PHONE");
        if (phoneData == null) return;

        String phone = (String) phoneData.get("phone");
        if (phone != null && !phone.isBlank()) {
            user.setPhone(phone);
        }
    }

    @SuppressWarnings("unchecked")
    private void persistNameStep(User user, Map<String, Object> draftData) {
        Map<String, Object> nameData = getStepData(draftData, "NAME");
        if (nameData == null) return;

        String username = (String) nameData.get("username");
        if (username != null && !username.isBlank()) {
            user.setUsername(username.trim());
        }

        String displayName = (String) nameData.get("displayName");
        if (displayName != null && !displayName.isBlank()) {
            user.setFullName(displayName.trim());
        }

        String profileType = (String) nameData.get("profileType");
        if (profileType != null && !profileType.isBlank()) {
            user.setProfileType(profileType.trim());
        }
    }

    @SuppressWarnings("unchecked")
    private void persistImageStep(User user, Map<String, Object> draftData) {
        Map<String, Object> imageData = getStepData(draftData, "IMAGE");
        if (imageData == null) return;

        String profilePicUrl = (String) imageData.get("profilePicUrl");
        if (profilePicUrl != null && !profilePicUrl.isBlank()) {
            user.setProfilePic(profilePicUrl);
        }
    }

    @SuppressWarnings("unchecked")
    private void persistOccupationStep(UUID userId, Map<String, Object> draftData) {
        Map<String, Object> occData = getStepData(draftData, "OCCUPATION");
        if (occData == null || occData.isEmpty()) return;

        // Check if a current occupation already exists
        var builder = UserOccupation.builder()
                .userId(userId)
                .isCurrent(true);

        String jobTitle = (String) occData.get("jobTitle");
        if (jobTitle != null && !jobTitle.isBlank()) {
            builder.jobTitle(jobTitle.trim());
        }

        String companyName = (String) occData.get("companyName");
        if (companyName != null && !companyName.isBlank()) {
            builder.companyName(companyName.trim());
        }

        Object skillsObj = occData.get("skills");
        if (skillsObj instanceof List<?> skillsList) {
            List<String> skills = skillsList.stream()
                    .filter(s -> s instanceof String)
                    .map(s -> ((String) s).trim())
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.toList());
            builder.skills(skills);
        }

        userOccupationRepository.save(builder.build());
    }

    @SuppressWarnings("unchecked")
    private void persistWealthStep(UUID userId, Map<String, Object> draftData) {
        Map<String, Object> wealthData = getStepData(draftData, "WEALTH");
        if (wealthData == null || wealthData.isEmpty()) return;

        var builder = UserWealth.builder()
                .userId(userId);

        String incomeBracket = (String) wealthData.get("incomeBracket");
        if (incomeBracket != null && !incomeBracket.isBlank()) {
            builder.incomeBracket(incomeBracket.trim());
        }

        Object savingPct = wealthData.get("monthlySavingPct");
        if (savingPct instanceof Number num) {
            builder.monthlySavingPct(num.intValue());
        }

        String currency = (String) wealthData.get("incomeCurrency");
        if (currency != null && !currency.isBlank()) {
            builder.incomeCurrency(currency.trim().toUpperCase());
        }

        userWealthRepository.save(builder.build());
    }

    @SuppressWarnings("unchecked")
    private void persistPhysiqueStep(UUID userId, Map<String, Object> draftData) {
        Map<String, Object> physiqueData = getStepData(draftData, "PHYSIQUE");
        if (physiqueData == null || physiqueData.isEmpty()) return;

        var builder = UserPhysique.builder()
                .userId(userId);

        Object heightObj = physiqueData.get("heightCm");
        if (heightObj instanceof Number num) {
            builder.heightCm(BigDecimal.valueOf(num.doubleValue()));
        }

        Object weightObj = physiqueData.get("weightKg");
        if (weightObj instanceof Number num) {
            builder.weightKg(BigDecimal.valueOf(num.doubleValue()));
        }

        String fitnessLevel = (String) physiqueData.get("fitnessLevel");
        if (fitnessLevel != null && !fitnessLevel.isBlank()) {
            try {
                builder.fitnessLevel(FitnessLevel.valueOf(fitnessLevel.trim().toUpperCase()));
            } catch (IllegalArgumentException e) {
                log.warn("Invalid fitness level '{}' for user {}, skipping", fitnessLevel, userId);
            }
        }

        userPhysiqueRepository.save(builder.build());
    }

    @SuppressWarnings("unchecked")
    private void persistWisdomStep(UUID userId, Map<String, Object> draftData) {
        Map<String, Object> wisdomData = getStepData(draftData, "WISDOM");
        if (wisdomData == null || wisdomData.isEmpty()) return;

        var builder = UserWisdom.builder()
                .userId(userId);

        Object hobbiesObj = wisdomData.get("languages");
        if (hobbiesObj instanceof List<?> hobbiesList) {
            List<String> hobbies = hobbiesList.stream()
                    .filter(s -> s instanceof String)
                    .map(s -> ((String) s).trim())
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.toList());
            builder.hobbies(hobbies);
        }

        userWisdomRepository.save(builder.build());
    }

    @SuppressWarnings("unchecked")
    private void persistCharismaStep(UUID userId, Map<String, Object> draftData) {
        Map<String, Object> charismaData = getStepData(draftData, "CHARISMA");
        if (charismaData == null || charismaData.isEmpty()) return;

        var builder = UserCharisma.builder()
                .userId(userId);

        String relationshipStatus = (String) charismaData.get("relationshipStatus");
        if (relationshipStatus != null && !relationshipStatus.isBlank()) {
            builder.relationshipStatus(relationshipStatus.trim());
        }

        Object socialCircleSize = charismaData.get("socialCircleSize");
        if (socialCircleSize instanceof Number num) {
            builder.socialCircleSize(num.intValue());
        }

        userCharismaRepository.save(builder.build());
    }

    // ===================== Helpers =====================

    @SuppressWarnings("unchecked")
    private Map<String, Object> getStepData(Map<String, Object> draftData, String stepKey) {
        Object data = draftData.get(stepKey);
        if (data instanceof Map) {
            return (Map<String, Object>) data;
        }
        return null;
    }
}
