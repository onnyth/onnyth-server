package com.onnyth.onnythserver.unit.service;

import com.onnyth.onnythserver.exceptions.IncompleteRegistrationException;
import com.onnyth.onnythserver.exceptions.UserNotFoundException;
import com.onnyth.onnythserver.models.*;
import com.onnyth.onnythserver.repository.*;
import com.onnyth.onnythserver.service.RegistrationCommitService;
import com.onnyth.onnythserver.service.RegistrationService;
import com.onnyth.onnythserver.support.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for RegistrationCommitService.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("RegistrationCommitService")
class RegistrationCommitServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private UserOccupationRepository userOccupationRepository;
    @Mock
    private UserWealthRepository userWealthRepository;
    @Mock
    private UserPhysiqueRepository userPhysiqueRepository;
    @Mock
    private UserWisdomRepository userWisdomRepository;
    @Mock
    private UserCharismaRepository userCharismaRepository;
    @Mock
    private RegistrationService registrationService;

    @InjectMocks
    private RegistrationCommitService commitService;

    private UUID userId;
    private User user;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        user = TestDataFactory.aUser().id(userId).email("test@test.com").build();
    }

    /**
     * Build a fully-populated draft with all required steps + some optional steps.
     */
    private RegistrationDraft buildCompleteDraft() {
        RegistrationDraft draft = RegistrationDraft.builder()
                .userId(userId)
                .currentStep(RegistrationStep.CHARISMA)
                .build();

        draft.mergeStepData(RegistrationStep.PHONE, Map.of("phone", "+1234567890"));
        draft.mergeStepData(RegistrationStep.NAME, Map.of(
                "username", "testuser",
                "displayName", "Test User",
                "profileType", "personal"));
        draft.mergeStepData(RegistrationStep.IMAGE, Map.of(
                "profilePicUrl", "https://storage.example.com/pic.jpg"));
        draft.mergeStepData(RegistrationStep.OCCUPATION, Map.of(
                "jobTitle", "Software Engineer",
                "companyName", "Onnyth",
                "skills", List.of("Java", "Spring")));
        draft.mergeStepData(RegistrationStep.WEALTH, Map.of(
                "incomeBracket", "50K-100K",
                "monthlySavingPct", 20));
        draft.mergeStepData(RegistrationStep.PHYSIQUE, Map.of(
                "heightCm", 180.0,
                "weightKg", 75.5,
                "fitnessLevel", "INTERMEDIATE"));
        draft.mergeStepData(RegistrationStep.WISDOM, Map.of(
                "languages", List.of("English", "Spanish")));
        draft.mergeStepData(RegistrationStep.CHARISMA, Map.of(
                "relationshipStatus", "single",
                "socialCircleSize", 150));

        return draft;
    }

    // ─── commitRegistration() ────────────────────────────────────────────────

    @Nested
    @DisplayName("commitRegistration()")
    class CommitRegistration {

        @Test
        @DisplayName("commits all steps and marks profile complete")
        void commitsAllSteps() {
            RegistrationDraft draft = buildCompleteDraft();

            when(registrationService.getDraft(userId)).thenReturn(Optional.of(draft));
            when(userRepository.findById(userId)).thenReturn(Optional.of(user));
            when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

            User result = commitService.commitRegistration(userId);

            assertThat(result.getProfileComplete()).isTrue();
            assertThat(result.getUsername()).isEqualTo("testuser");
            assertThat(result.getFullName()).isEqualTo("Test User");
            assertThat(result.getPhone()).isEqualTo("+1234567890");
            assertThat(result.getProfilePic()).isEqualTo("https://storage.example.com/pic.jpg");
            assertThat(result.getProfileType()).isEqualTo("personal");

            verify(userOccupationRepository).save(any(UserOccupation.class));
            verify(userWealthRepository).save(any(UserWealth.class));
            verify(userPhysiqueRepository).save(any(UserPhysique.class));
            verify(userWisdomRepository).save(any(UserWisdom.class));
            verify(userCharismaRepository).save(any(UserCharisma.class));
            verify(registrationService).deleteDraft(userId);
        }

        @Test
        @DisplayName("throws IllegalStateException when no draft exists")
        void throwsWhenNoDraft() {
            when(registrationService.getDraft(userId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> commitService.commitRegistration(userId))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("No registration draft found");
        }

        @Test
        @DisplayName("throws IncompleteRegistrationException when required steps are missing")
        void throwsWhenRequiredStepsMissing() {
            // Draft with only PHONE step (NAME is also required)
            RegistrationDraft draft = RegistrationDraft.builder()
                    .userId(userId)
                    .currentStep(RegistrationStep.NAME)
                    .build();
            draft.mergeStepData(RegistrationStep.PHONE, Map.of("phone", "+1234567890"));

            when(registrationService.getDraft(userId)).thenReturn(Optional.of(draft));

            assertThatThrownBy(() -> commitService.commitRegistration(userId))
                    .isInstanceOf(IncompleteRegistrationException.class);
        }

        @Test
        @DisplayName("throws UserNotFoundException when user doesn't exist in DB")
        void throwsWhenUserNotFound() {
            RegistrationDraft draft = buildCompleteDraft();

            when(registrationService.getDraft(userId)).thenReturn(Optional.of(draft));
            when(userRepository.findById(userId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> commitService.commitRegistration(userId))
                    .isInstanceOf(UserNotFoundException.class);
        }

        @Test
        @DisplayName("skips optional domain tables when their data is empty")
        void skipsOptionalDomains_whenEmpty() {
            // Only the two required steps: PHONE and NAME
            RegistrationDraft draft = RegistrationDraft.builder()
                    .userId(userId)
                    .currentStep(RegistrationStep.NAME)
                    .build();
            draft.mergeStepData(RegistrationStep.PHONE, Map.of("phone", "+1234567890"));
            draft.mergeStepData(RegistrationStep.NAME, Map.of(
                    "username", "testuser",
                    "displayName", "Test User"));

            when(registrationService.getDraft(userId)).thenReturn(Optional.of(draft));
            when(userRepository.findById(userId)).thenReturn(Optional.of(user));
            when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

            User result = commitService.commitRegistration(userId);

            assertThat(result.getProfileComplete()).isTrue();
            // Domain repos should never be called since there's no data
            verify(userOccupationRepository, never()).save(any());
            verify(userWealthRepository, never()).save(any());
            verify(userPhysiqueRepository, never()).save(any());
            verify(userWisdomRepository, never()).save(any());
            verify(userCharismaRepository, never()).save(any());
        }

        @Test
        @DisplayName("persists occupation skills as list")
        void persistsOccupationSkills() {
            RegistrationDraft draft = buildCompleteDraft();

            when(registrationService.getDraft(userId)).thenReturn(Optional.of(draft));
            when(userRepository.findById(userId)).thenReturn(Optional.of(user));
            when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

            commitService.commitRegistration(userId);

            verify(userOccupationRepository).save(argThat(occ ->
                    occ.getSkills().size() == 2 &&
                    occ.getSkills().contains("Java") &&
                    occ.getSkills().contains("Spring") &&
                    occ.getJobTitle().equals("Software Engineer")));
        }
    }
}
