package com.onnyth.onnythserver.unit.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.onnyth.onnythserver.dto.registration.RegistrationStatusResponse;
import com.onnyth.onnythserver.dto.registration.RegistrationStepResponse;
import com.onnyth.onnythserver.exceptions.UsernameAlreadyExistsException;
import com.onnyth.onnythserver.models.RegistrationDraft;
import com.onnyth.onnythserver.models.RegistrationStep;
import com.onnyth.onnythserver.models.User;
import com.onnyth.onnythserver.repository.RegistrationDraftRepository;
import com.onnyth.onnythserver.repository.UserRepository;
import com.onnyth.onnythserver.service.RegistrationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for RegistrationService — draft CRUD, step validation, step progression.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("RegistrationService")
class RegistrationServiceTest {

    @Mock(lenient = true)
    private RegistrationDraftRepository draftRepository;

    @Mock(lenient = true)
    private UserRepository userRepository;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private RegistrationService registrationService;

    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
    }

    // ─── getStatus() ─────────────────────────────────────────────────────────

    @Nested
    @DisplayName("getStatus()")
    class GetStatus {

        @Test
        @DisplayName("returns empty status when no draft exists")
        void returnsEmptyStatus_whenNoDraft() {
            when(draftRepository.findById(userId)).thenReturn(Optional.empty());

            RegistrationStatusResponse response = registrationService.getStatus(userId);

            assertThat(response.currentStep()).isEqualTo("PHONE");
            assertThat(response.completedSteps()).isEmpty();
            assertThat(response.draftData()).isNull();
        }

        @Test
        @DisplayName("returns draft status when draft exists")
        void returnsDraftStatus() {
            RegistrationDraft draft = RegistrationDraft.builder()
                    .userId(userId)
                    .currentStep(RegistrationStep.NAME)
                    .build();
            draft.mergeStepData(RegistrationStep.PHONE, Map.of("phone", "+1234567890"));

            when(draftRepository.findById(userId)).thenReturn(Optional.of(draft));

            RegistrationStatusResponse response = registrationService.getStatus(userId);

            assertThat(response.currentStep()).isEqualTo("NAME");
            assertThat(response.completedSteps()).containsExactly("PHONE");
            assertThat(response.draftData()).containsKey("PHONE");
        }
    }

    // ─── saveStep() ──────────────────────────────────────────────────────────

    @Nested
    @DisplayName("saveStep()")
    class SaveStep {

        @Test
        @DisplayName("creates new draft when none exists")
        void createsNewDraft_whenNoneExists() {
            when(draftRepository.findById(userId)).thenReturn(Optional.empty());
            when(draftRepository.save(any(RegistrationDraft.class))).thenAnswer(inv -> inv.getArgument(0));

            Map<String, Object> stepData = Map.of("phone", "+1234567890");
            RegistrationStepResponse response = registrationService.saveStep(
                    userId, RegistrationStep.PHONE, stepData);

            assertThat(response.currentStep()).isEqualTo("NAME"); // advanced to next
            assertThat(response.completedSteps()).containsExactly("PHONE");
            verify(draftRepository).save(any(RegistrationDraft.class));
        }

        @Test
        @DisplayName("merges data into existing draft")
        void mergesIntoExistingDraft() {
            RegistrationDraft existing = RegistrationDraft.builder()
                    .userId(userId)
                    .currentStep(RegistrationStep.NAME)
                    .build();
            existing.mergeStepData(RegistrationStep.PHONE, Map.of("phone", "+1234567890"));

            when(draftRepository.findById(userId)).thenReturn(Optional.of(existing));
            when(draftRepository.save(any(RegistrationDraft.class))).thenAnswer(inv -> inv.getArgument(0));
            when(userRepository.existsByUsernameIgnoreCase("testuser")).thenReturn(false);

            Map<String, Object> stepData = Map.of(
                    "username", "testuser",
                    "displayName", "Test User",
                    "profileType", "personal");

            RegistrationStepResponse response = registrationService.saveStep(
                    userId, RegistrationStep.NAME, stepData);

            assertThat(response.currentStep()).isEqualTo("IMAGE"); // advanced
            assertThat(response.completedSteps()).contains("PHONE", "NAME");
        }

        @Test
        @DisplayName("keeps current step as CHARISMA on last step")
        void keepsCurrentStep_onLastStep() {
            RegistrationDraft existing = RegistrationDraft.builder()
                    .userId(userId)
                    .currentStep(RegistrationStep.CHARISMA)
                    .build();

            when(draftRepository.findById(userId)).thenReturn(Optional.of(existing));
            when(draftRepository.save(any(RegistrationDraft.class))).thenAnswer(inv -> inv.getArgument(0));

            Map<String, Object> stepData = Map.of("relationshipStatus", "single");
            RegistrationStepResponse response = registrationService.saveStep(
                    userId, RegistrationStep.CHARISMA, stepData);

            assertThat(response.currentStep()).isEqualTo("CHARISMA");
        }
    }

    // ─── Step Validation ─────────────────────────────────────────────────────

    @Nested
    @DisplayName("step validation")
    class StepValidation {

        @Test
        @DisplayName("throws on missing username in NAME step")
        void throwsOnMissingUsername() {
            when(draftRepository.findById(userId)).thenReturn(Optional.empty());

            Map<String, Object> stepData = Map.of("displayName", "Test");

            assertThatThrownBy(() -> registrationService.saveStep(
                    userId, RegistrationStep.NAME, stepData))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Username is required");
        }

        @Test
        @DisplayName("throws on missing displayName in NAME step")
        void throwsOnMissingDisplayName() {
            when(draftRepository.findById(userId)).thenReturn(Optional.empty());
            when(userRepository.existsByUsernameIgnoreCase("testuser")).thenReturn(false);

            Map<String, Object> stepData = Map.of("username", "testuser");

            assertThatThrownBy(() -> registrationService.saveStep(
                    userId, RegistrationStep.NAME, stepData))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Display name is required");
        }

        @Test
        @DisplayName("throws UsernameAlreadyExistsException on taken username")
        void throwsOnTakenUsername() {
            when(draftRepository.findById(userId)).thenReturn(Optional.empty());
            when(userRepository.existsByUsernameIgnoreCase("taken")).thenReturn(true);
            when(userRepository.findById(userId)).thenReturn(Optional.empty());

            Map<String, Object> stepData = Map.of(
                    "username", "taken",
                    "displayName", "Test");

            assertThatThrownBy(() -> registrationService.saveStep(
                    userId, RegistrationStep.NAME, stepData))
                    .isInstanceOf(UsernameAlreadyExistsException.class);
        }

        @Test
        @DisplayName("allows re-use of own existing username")
        void allowsOwnExistingUsername() {
            User existingUser = User.builder().id(userId).email("t@t.com").username("myname").build();

            when(draftRepository.findById(userId)).thenReturn(Optional.empty());
            when(userRepository.existsByUsernameIgnoreCase("myname")).thenReturn(true);
            when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
            when(draftRepository.save(any(RegistrationDraft.class))).thenAnswer(inv -> inv.getArgument(0));

            Map<String, Object> stepData = Map.of(
                    "username", "myname",
                    "displayName", "My Name");

            // Should not throw
            RegistrationStepResponse response = registrationService.saveStep(
                    userId, RegistrationStep.NAME, stepData);

            assertThat(response.completedSteps()).contains("NAME");
        }

        @Test
        @DisplayName("throws on missing phone in PHONE step")
        void throwsOnMissingPhone() {
            when(draftRepository.findById(userId)).thenReturn(Optional.empty());

            Map<String, Object> stepData = new HashMap<>();

            assertThatThrownBy(() -> registrationService.saveStep(
                    userId, RegistrationStep.PHONE, stepData))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Phone number is required");
        }

        @Test
        @DisplayName("optional steps pass without validation")
        void optionalStepsPass() {
            when(draftRepository.findById(userId)).thenReturn(Optional.empty());
            when(draftRepository.save(any(RegistrationDraft.class))).thenAnswer(inv -> inv.getArgument(0));

            Map<String, Object> stepData = Map.of("jobTitle", "Engineer");

            // Should not throw — OCCUPATION is optional
            RegistrationStepResponse response = registrationService.saveStep(
                    userId, RegistrationStep.OCCUPATION, stepData);

            assertThat(response).isNotNull();
        }
    }

    // ─── saveImageStep() ─────────────────────────────────────────────────────

    @Nested
    @DisplayName("saveImageStep()")
    class SaveImageStep {

        @Test
        @DisplayName("creates image step with profilePicUrl")
        void savesImageStep() {
            when(draftRepository.findById(userId)).thenReturn(Optional.empty());
            when(draftRepository.save(any(RegistrationDraft.class))).thenAnswer(inv -> inv.getArgument(0));

            RegistrationStepResponse response = registrationService.saveImageStep(
                    userId, "https://storage.example.com/pic.jpg");

            assertThat(response.completedSteps()).contains("IMAGE");
            verify(draftRepository).save(any(RegistrationDraft.class));
        }
    }

    // ─── getDraft() / deleteDraft() ──────────────────────────────────────────

    @Nested
    @DisplayName("getDraft() / deleteDraft()")
    class DraftLifecycle {

        @Test
        @DisplayName("getDraft returns empty when no draft")
        void getDraft_returnsEmpty() {
            when(draftRepository.findById(userId)).thenReturn(Optional.empty());

            assertThat(registrationService.getDraft(userId)).isEmpty();
        }

        @Test
        @DisplayName("deleteDraft calls repository deleteById")
        void deleteDraft_callsDelete() {
            registrationService.deleteDraft(userId);

            verify(draftRepository).deleteById(userId);
        }
    }
}
