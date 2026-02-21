package com.onnyth.onnythserver.unit.service;

import com.onnyth.onnythserver.dto.ProfileResponse;
import com.onnyth.onnythserver.dto.ProfileUpdateRequest;
import com.onnyth.onnythserver.exceptions.UserNotFoundException;
import com.onnyth.onnythserver.exceptions.UsernameAlreadyExistsException;
import com.onnyth.onnythserver.models.User;
import com.onnyth.onnythserver.repository.UserRepository;
import com.onnyth.onnythserver.service.ProfileService;
import com.onnyth.onnythserver.service.StorageService;
import com.onnyth.onnythserver.support.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ProfileService.
 */
@ExtendWith(MockitoExtension.class)
class ProfileServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private StorageService storageService;

    @InjectMocks
    private ProfileService profileService;

    private UUID userId;
    private User testUser;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        testUser = TestDataFactory.aUser()
                .id(userId)
                .username("currentuser")
                .fullName("Current User")
                .profilePic("https://example.com/old.jpg")
                .build();
    }

    // ─── getProfile() ─────────────────────────────────────────────────────────

    @Nested
    @DisplayName("getProfile()")
    class GetProfile {

        @Test
        @DisplayName("returns ProfileResponse when user exists")
        void returnsProfile_whenUserExists() {
            when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));

            ProfileResponse result = profileService.getProfile(userId);

            assertThat(result.id()).isEqualTo(userId);
            assertThat(result.username()).isEqualTo("currentuser");
        }

        @Test
        @DisplayName("throws UserNotFoundException when user does not exist")
        void throwsUserNotFound_whenUserMissing() {
            when(userRepository.findById(userId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> profileService.getProfile(userId))
                    .isInstanceOf(UserNotFoundException.class);
        }
    }

    // ─── updateProfile() ──────────────────────────────────────────────────────

    @Nested
    @DisplayName("updateProfile()")
    class UpdateProfile {

        @Test
        @DisplayName("updates username when new unique username is provided")
        void updatesUsername_whenUniqueUsernameProvided() {
            ProfileUpdateRequest request = new ProfileUpdateRequest("newusername", null, null);

            when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
            when(userRepository.existsByUsernameIgnoreCase("newusername")).thenReturn(false);
            when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

            ProfileResponse result = profileService.updateProfile(userId, request);

            assertThat(result.username()).isEqualTo("newusername");
        }

        @Test
        @DisplayName("allows user to keep their own username (case-insensitive)")
        void allowsKeepingOwnUsername() {
            ProfileUpdateRequest request = new ProfileUpdateRequest("CURRENTUSER", null, null);

            when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
            when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

            // Should NOT check existsByUsernameIgnoreCase for own username
            ProfileResponse result = profileService.updateProfile(userId, request);

            assertThat(result.username()).isEqualTo("CURRENTUSER");
            verify(userRepository, never()).existsByUsernameIgnoreCase(anyString());
        }

        @Test
        @DisplayName("throws UsernameAlreadyExistsException when username is taken by another user")
        void throwsUsernameAlreadyExists_whenTakenByOther() {
            ProfileUpdateRequest request = new ProfileUpdateRequest("takenuser", null, null);

            when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
            when(userRepository.existsByUsernameIgnoreCase("takenuser")).thenReturn(true);

            assertThatThrownBy(() -> profileService.updateProfile(userId, request))
                    .isInstanceOf(UsernameAlreadyExistsException.class);
        }

        @Test
        @DisplayName("updates fullName when provided")
        void updatesFullName_whenProvided() {
            ProfileUpdateRequest request = new ProfileUpdateRequest(null, "New Full Name", null);

            when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
            when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

            ProfileResponse result = profileService.updateProfile(userId, request);

            assertThat(result.fullName()).isEqualTo("New Full Name");
        }

        @Test
        @DisplayName("updates profilePic URL when provided")
        void updatesProfilePic_whenProvided() {
            ProfileUpdateRequest request = new ProfileUpdateRequest(null, null, "https://example.com/new.jpg");

            when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
            when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

            ProfileResponse result = profileService.updateProfile(userId, request);

            assertThat(result.profilePic()).isEqualTo("https://example.com/new.jpg");
        }

        @Test
        @DisplayName("throws UserNotFoundException when user does not exist")
        void throwsUserNotFound_whenUserMissing() {
            when(userRepository.findById(userId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> profileService.updateProfile(userId, new ProfileUpdateRequest(null, null, null)))
                    .isInstanceOf(UserNotFoundException.class);
        }

        @Test
        @DisplayName("skips username update when username is blank")
        void skipsUsernameUpdate_whenBlank() {
            ProfileUpdateRequest request = new ProfileUpdateRequest("   ", "New Name", null);

            when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
            when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

            ProfileResponse result = profileService.updateProfile(userId, request);

            // Username should remain unchanged
            assertThat(result.username()).isEqualTo("currentuser");
        }
    }

    // ─── isUsernameAvailable() ────────────────────────────────────────────────

    @Nested
    @DisplayName("isUsernameAvailable()")
    class IsUsernameAvailable {

        @Test
        @DisplayName("returns true when username is not taken")
        void returnsTrue_whenUsernameAvailable() {
            when(userRepository.existsByUsernameIgnoreCase("freshname")).thenReturn(false);

            boolean result = profileService.isUsernameAvailable("freshname", null);

            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("returns false when username is taken")
        void returnsFalse_whenUsernameTaken() {
            when(userRepository.existsByUsernameIgnoreCase("takenname")).thenReturn(true);

            boolean result = profileService.isUsernameAvailable("takenname", null);

            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("returns true when username belongs to the current user")
        void returnsTrue_whenUsernameIsOwnersUsername() {
            when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));

            boolean result = profileService.isUsernameAvailable("currentuser", userId);

            assertThat(result).isTrue();
            verify(userRepository, never()).existsByUsernameIgnoreCase(anyString());
        }

        @Test
        @DisplayName("returns false when username is null")
        void returnsFalse_whenUsernameNull() {
            boolean result = profileService.isUsernameAvailable(null, null);

            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("returns false when username is blank")
        void returnsFalse_whenUsernameBlank() {
            boolean result = profileService.isUsernameAvailable("   ", null);

            assertThat(result).isFalse();
        }
    }

    // ─── uploadProfilePicture() ───────────────────────────────────────────────

    @Nested
    @DisplayName("uploadProfilePicture()")
    class UploadProfilePicture {

        @Test
        @DisplayName("uploads picture and updates user profile")
        void uploadsAndUpdatesProfile() {
            byte[] imageData = new byte[1024];
            String newUrl = "https://example.com/new-pic.jpg";

            when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
            when(storageService.uploadProfilePicture(eq(userId), eq(imageData), eq("image/jpeg"), eq("photo.jpg")))
                    .thenReturn(newUrl);
            when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

            ProfileResponse result = profileService.uploadProfilePicture(userId, imageData, "image/jpeg", "photo.jpg");

            assertThat(result.profilePic()).isEqualTo(newUrl);
        }

        @Test
        @DisplayName("deletes old profile picture before uploading new one")
        void deletesOldPicture_beforeUpload() {
            byte[] imageData = new byte[1024];
            String oldUrl = "https://example.com/old.jpg";
            testUser.setProfilePic(oldUrl);

            when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
            when(storageService.uploadProfilePicture(any(), any(), any(), any()))
                    .thenReturn("https://example.com/new.jpg");
            when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

            profileService.uploadProfilePicture(userId, imageData, "image/jpeg", "photo.jpg");

            verify(storageService).deleteFile(oldUrl);
        }

        @Test
        @DisplayName("does not call deleteFile when user has no existing profile picture")
        void doesNotDeleteFile_whenNoPreviousPicture() {
            testUser.setProfilePic(null);
            byte[] imageData = new byte[1024];

            when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
            when(storageService.uploadProfilePicture(any(), any(), any(), any()))
                    .thenReturn("https://example.com/new.jpg");
            when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

            profileService.uploadProfilePicture(userId, imageData, "image/jpeg", "photo.jpg");

            verify(storageService, never()).deleteFile(anyString());
        }

        @Test
        @DisplayName("throws UserNotFoundException when user does not exist")
        void throwsUserNotFound_whenUserMissing() {
            when(userRepository.findById(userId)).thenReturn(Optional.empty());

            assertThatThrownBy(() ->
                    profileService.uploadProfilePicture(userId, new byte[1024], "image/jpeg", "photo.jpg"))
                    .isInstanceOf(UserNotFoundException.class);
        }
    }

    // ─── getProfileCard() ─────────────────────────────────────────────────────

    @Nested
    @DisplayName("getProfileCard()")
    class GetProfileCard {

        @Test
        @DisplayName("returns ProfileCardResponse when user exists")
        void returnsProfileCard_whenUserExists() {
            when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));

            var result = profileService.getProfileCard(userId);

            assertThat(result.userId()).isEqualTo(userId);
            assertThat(result.username()).isEqualTo("currentuser");
            assertThat(result.fullName()).isEqualTo("Current User");
            assertThat(result.profilePic()).isEqualTo("https://example.com/old.jpg");
            assertThat(result.totalScore()).isEqualTo(0); // default until LifeStats integration
            assertThat(result.rankTier()).isEqualTo("Novice");
            assertThat(result.rankBadgeUrl()).isEqualTo("🟤");
        }

        @Test
        @DisplayName("throws UserNotFoundException when user does not exist")
        void throwsUserNotFound_whenUserMissing() {
            when(userRepository.findById(userId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> profileService.getProfileCard(userId))
                    .isInstanceOf(UserNotFoundException.class);
        }
    }
}
