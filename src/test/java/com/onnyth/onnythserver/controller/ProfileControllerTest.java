package com.onnyth.onnythserver.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.onnyth.onnythserver.dto.ProfileCardResponse;
import com.onnyth.onnythserver.dto.ProfileResponse;
import com.onnyth.onnythserver.dto.ProfileUpdateRequest;
import com.onnyth.onnythserver.exceptions.UserNotFoundException;
import com.onnyth.onnythserver.exceptions.UsernameAlreadyExistsException;
import com.onnyth.onnythserver.service.ProfileService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.onnyth.onnythserver.security.SecurityConfig;
import com.onnyth.onnythserver.support.MockJwtDecoderConfig;

import java.time.Instant;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Web layer tests for ProfileController.
 * Tests JWT extraction, routing, validation, and multipart upload.
 */
@WebMvcTest(ProfileController.class)
@Import({ SecurityConfig.class, MockJwtDecoderConfig.class })
class ProfileControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        @MockitoBean
        private ProfileService profileService;

        private static final UUID USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");

        private ProfileResponse buildProfileResponse() {
                return ProfileResponse.builder()
                                .id(USER_ID)
                                .email("user@example.com")
                                .username("testuser")
                                .fullName("Test User")
                                .profilePic("https://example.com/pic.jpg")
                                .emailVerified(true)
                                .profileComplete(true)
                                .createdAt(Instant.now())
                                .updatedAt(Instant.now())
                                .build();
        }

        // ─── GET /api/v1/profile ──────────────────────────────────────────────────

        @Nested
        @DisplayName("GET /api/v1/profile")
        class GetProfile {

        @Test
        @DisplayName("returns 200 with profile when authenticated")
        void returns200_whenAuthenticated() throws Exception {
            when(profileService.getProfile(USER_ID)).thenReturn(buildProfileResponse());

            mockMvc.perform(get("/api/v1/profile")
                            .with(jwt().jwt(j -> j.subject(USER_ID.toString()))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(USER_ID.toString()))
                    .andExpect(jsonPath("$.username").value("testuser"))
                    .andExpect(jsonPath("$.email").value("user@example.com"));
        }

                @Test
                @DisplayName("returns 401 when not authenticated")
                void returns401_whenNotAuthenticated() throws Exception {
                        mockMvc.perform(get("/api/v1/profile"))
                                        .andExpect(status().isUnauthorized());
                }

        @Test
        @DisplayName("returns 404 when user not found")
        void returns404_whenUserNotFound() throws Exception {
            when(profileService.getProfile(USER_ID))
                    .thenThrow(new UserNotFoundException(USER_ID.toString()));

            mockMvc.perform(get("/api/v1/profile")
                            .with(jwt().jwt(j -> j.subject(USER_ID.toString()))))
                    .andExpect(status().isNotFound());
        }
        }

        // ─── PUT /api/v1/profile ──────────────────────────────────────────────────

        @Nested
        @DisplayName("PUT /api/v1/profile")
        class UpdateProfile {

                @Test
                @DisplayName("returns 200 with updated profile on success")
                void returns200_onSuccess() throws Exception {
                        ProfileUpdateRequest request = new ProfileUpdateRequest("newusername", "New Name", null);
                        when(profileService.updateProfile(eq(USER_ID), any(ProfileUpdateRequest.class)))
                                        .thenReturn(buildProfileResponse());

                        mockMvc.perform(put("/api/v1/profile")
                                        .with(jwt().jwt(j -> j.subject(USER_ID.toString())))
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(request)))
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath("$.username").value("testuser"));
                }

                @Test
                @DisplayName("returns 400 when username fails validation (too short)")
                void returns400_whenUsernameInvalid() throws Exception {
                        ProfileUpdateRequest request = new ProfileUpdateRequest("ab", "Name", null); // < 3 chars

                        mockMvc.perform(put("/api/v1/profile")
                                        .with(jwt().jwt(j -> j.subject(USER_ID.toString())))
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(request)))
                                        .andExpect(status().isBadRequest());
                }

                @Test
                @DisplayName("returns 400 when username contains invalid characters")
                void returns400_whenUsernameHasSpecialChars() throws Exception {
                        ProfileUpdateRequest request = new ProfileUpdateRequest("user@name!", "Name", null);

                        mockMvc.perform(put("/api/v1/profile")
                                        .with(jwt().jwt(j -> j.subject(USER_ID.toString())))
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(request)))
                                        .andExpect(status().isBadRequest());
                }

                @Test
                @DisplayName("returns 409 when username is already taken")
                void returns409_whenUsernameTaken() throws Exception {
                        ProfileUpdateRequest request = new ProfileUpdateRequest("takenuser", "Name", null);
                        when(profileService.updateProfile(eq(USER_ID), any(ProfileUpdateRequest.class)))
                                        .thenThrow(new UsernameAlreadyExistsException("takenuser"));

                        mockMvc.perform(put("/api/v1/profile")
                                        .with(jwt().jwt(j -> j.subject(USER_ID.toString())))
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(request)))
                                        .andExpect(status().isConflict());
                }

                @Test
                @DisplayName("returns 401 when not authenticated")
                void returns401_whenNotAuthenticated() throws Exception {
                        mockMvc.perform(put("/api/v1/profile")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content("{}"))
                                        .andExpect(status().isUnauthorized());
                }
        }

        // ─── GET /api/v1/profile/check-username/{username} ───────────────────────

        @Nested
        @DisplayName("GET /api/v1/profile/check-username/{username}")
        class CheckUsername {

        @Test
        @DisplayName("returns 200 with available=true when username is free")
        void returns200_whenUsernameAvailable() throws Exception {
            when(profileService.isUsernameAvailable(eq("freshname"), eq(USER_ID))).thenReturn(true);

            mockMvc.perform(get("/api/v1/profile/check-username/freshname")
                            .with(jwt().jwt(j -> j.subject(USER_ID.toString()))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.username").value("freshname"))
                    .andExpect(jsonPath("$.available").value(true));
        }

        @Test
        @DisplayName("returns 200 with available=false when username is taken")
        void returns200_whenUsernameTaken() throws Exception {
            when(profileService.isUsernameAvailable(eq("takenname"), eq(USER_ID))).thenReturn(false);

            mockMvc.perform(get("/api/v1/profile/check-username/takenname")
                            .with(jwt().jwt(j -> j.subject(USER_ID.toString()))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.available").value(false));
        }
        }

        // ─── POST /api/v1/profile/picture ─────────────────────────────────────────

        @Nested
        @DisplayName("POST /api/v1/profile/picture")
        class UploadProfilePicture {

                @Test
                @DisplayName("returns 200 with updated profile on successful upload")
                void returns200_onSuccess() throws Exception {
                        MockMultipartFile file = new MockMultipartFile(
                                        "file", "photo.jpg", "image/jpeg", new byte[1024]);

                        when(profileService.uploadProfilePicture(eq(USER_ID), any(), eq("image/jpeg"), eq("photo.jpg")))
                                        .thenReturn(buildProfileResponse());

                        mockMvc.perform(multipart("/api/v1/profile/picture")
                                        .file(file)
                                        .with(jwt().jwt(j -> j.subject(USER_ID.toString()))))
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath("$.profilePic").value("https://example.com/pic.jpg"));
                }

                @Test
                @DisplayName("returns 401 when not authenticated")
                void returns401_whenNotAuthenticated() throws Exception {
                        MockMultipartFile file = new MockMultipartFile(
                                        "file", "photo.jpg", "image/jpeg", new byte[1024]);

                        mockMvc.perform(multipart("/api/v1/profile/picture").file(file))
                                        .andExpect(status().isUnauthorized());
                }
        }

        // ─── GET /api/v1/profile/card
        // ─────────────────────────────────────────────────────

        @Nested
        @DisplayName("GET /api/v1/profile/card")
        class GetProfileCard {

                @Test
                @DisplayName("returns 200 with profile card when authenticated")
                void returns200_withProfileCard() throws Exception {
                        ProfileCardResponse card = ProfileCardResponse.builder()
                                        .userId(USER_ID)
                                        .username("hero")
                                        .fullName("Test Hero")
                                        .profilePic("https://cdn.example.com/pic.jpg")
                                        .totalScore(0)
                                        .rankTier("Novice")
                                        .rankBadgeUrl("🟤")
                                        .build();

                        when(profileService.getProfileCard(USER_ID)).thenReturn(card);

                        mockMvc.perform(get("/api/v1/profile/card")
                                        .with(jwt().jwt(j -> j.subject(USER_ID.toString()))))
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath("$.userId").value(USER_ID.toString()))
                                        .andExpect(jsonPath("$.username").value("hero"))
                                        .andExpect(jsonPath("$.rankTier").value("Novice"))
                                        .andExpect(jsonPath("$.totalScore").value(0));
                }

                @Test
                @DisplayName("returns 401 when not authenticated")
                void returns401_whenNotAuthenticated() throws Exception {
                        mockMvc.perform(get("/api/v1/profile/card"))
                                        .andExpect(status().isUnauthorized());
                }

                @Test
                @DisplayName("returns 404 when user not found")
                void returns404_whenUserNotFound() throws Exception {
                        when(profileService.getProfileCard(USER_ID))
                                        .thenThrow(new UserNotFoundException(USER_ID.toString()));

                        mockMvc.perform(get("/api/v1/profile/card")
                                        .with(jwt().jwt(j -> j.subject(USER_ID.toString()))))
                                        .andExpect(status().isNotFound());
                }
        }
}
