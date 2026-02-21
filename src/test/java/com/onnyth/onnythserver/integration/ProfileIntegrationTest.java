package com.onnyth.onnythserver.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.onnyth.onnythserver.dto.ProfileUpdateRequest;
import com.onnyth.onnythserver.models.User;
import com.onnyth.onnythserver.repository.UserRepository;
import com.onnyth.onnythserver.support.PostgresTestContainer;
import com.onnyth.onnythserver.support.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.wiremock.spring.EnableWireMock;
import org.wiremock.spring.InjectWireMock;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for the Profile flow.
 * Tests the full stack: HTTP → Controller → Service → Repository → DB.
 * Storage calls (Supabase Storage) are stubbed via WireMock.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@EnableWireMock
class ProfileIntegrationTest extends PostgresTestContainer {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @InjectWireMock
    private com.github.tomakehurst.wiremock.WireMockServer wireMock;

    private UUID userId;
    private User savedUser;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        userId = UUID.randomUUID();
        savedUser = userRepository.save(
                TestDataFactory.aUser()
                        .id(userId)
                        .username("initialuser")
                        .fullName("Initial User")
                        .profilePic("https://example.com/old.jpg")
                        .build());
    }

    // ─── GET /api/v1/profile ──────────────────────────────────────────────────

    @Nested
    @DisplayName("GET /api/v1/profile")
    class GetProfile {

        @Test
        @DisplayName("returns correct profile for authenticated user")
        void returnsProfile_forAuthenticatedUser() throws Exception {
            mockMvc.perform(get("/api/v1/profile")
                    .with(jwt().jwt(j -> j.subject(userId.toString()))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(userId.toString()))
                    .andExpect(jsonPath("$.username").value("initialuser"))
                    .andExpect(jsonPath("$.fullName").value("Initial User"));
        }

        @Test
        @DisplayName("returns 404 when user does not exist in DB")
        void returns404_whenUserNotInDB() throws Exception {
            UUID unknownId = UUID.randomUUID();

            mockMvc.perform(get("/api/v1/profile")
                    .with(jwt().jwt(j -> j.subject(unknownId.toString()))))
                    .andExpect(status().isNotFound());
        }
    }

    // ─── PUT /api/v1/profile ──────────────────────────────────────────────────

    @Nested
    @DisplayName("PUT /api/v1/profile")
    class UpdateProfile {

        @Test
        @DisplayName("updates username and persists to DB")
        void updatesUsername_andPersistsToDB() throws Exception {
            ProfileUpdateRequest request = new ProfileUpdateRequest("updateduser", "Updated Name", null);

            mockMvc.perform(put("/api/v1/profile")
                    .with(jwt().jwt(j -> j.subject(userId.toString())))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.username").value("updateduser"))
                    .andExpect(jsonPath("$.fullName").value("Updated Name"));

            // Verify persisted in DB
            User updatedUser = userRepository.findById(userId).orElseThrow();
            assertThat(updatedUser.getUsername()).isEqualTo("updateduser");
            assertThat(updatedUser.getFullName()).isEqualTo("Updated Name");
        }

        @Test
        @DisplayName("returns 409 when username is already taken by another user")
        void returns409_whenUsernameTaken() throws Exception {
            // Create another user with the target username
            userRepository.save(TestDataFactory.aUser().username("takenuser").build());

            ProfileUpdateRequest request = new ProfileUpdateRequest("takenuser", null, null);

            mockMvc.perform(put("/api/v1/profile")
                    .with(jwt().jwt(j -> j.subject(userId.toString())))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isConflict());
        }

        @Test
        @DisplayName("allows user to update other fields without changing username")
        void allowsUpdatingOtherFields_withoutChangingUsername() throws Exception {
            ProfileUpdateRequest request = new ProfileUpdateRequest(null, "New Full Name", null);

            mockMvc.perform(put("/api/v1/profile")
                    .with(jwt().jwt(j -> j.subject(userId.toString())))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.username").value("initialuser")) // unchanged
                    .andExpect(jsonPath("$.fullName").value("New Full Name"));
        }

        @Test
        @DisplayName("marks profile as complete when all required fields are set")
        void marksProfileComplete_whenAllFieldsSet() throws Exception {
            ProfileUpdateRequest request = new ProfileUpdateRequest(
                    "completeuser", "Complete User", "https://example.com/pic.jpg");

            mockMvc.perform(put("/api/v1/profile")
                    .with(jwt().jwt(j -> j.subject(userId.toString())))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.profileComplete").value(true));

            User updatedUser = userRepository.findById(userId).orElseThrow();
            assertThat(updatedUser.getProfileComplete()).isTrue();
        }
    }

    // ─── GET /api/v1/profile/check-username/{username} ───────────────────────

    @Nested
    @DisplayName("GET /api/v1/profile/check-username/{username}")
    class CheckUsername {

        @Test
        @DisplayName("returns available=true for a free username")
        void returnsAvailable_forFreeUsername() throws Exception {
            mockMvc.perform(get("/api/v1/profile/check-username/brandnewname")
                    .with(jwt().jwt(j -> j.subject(userId.toString()))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.available").value(true));
        }

        @Test
        @DisplayName("returns available=false for a taken username")
        void returnsUnavailable_forTakenUsername() throws Exception {
            // "initialuser" is already taken by savedUser
            mockMvc.perform(get("/api/v1/profile/check-username/initialuser")
                    .with(jwt().jwt(j -> j.subject(UUID.randomUUID().toString()))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.available").value(false));
        }

        @Test
        @DisplayName("returns available=true when checking own username")
        void returnsAvailable_forOwnUsername() throws Exception {
            mockMvc.perform(get("/api/v1/profile/check-username/initialuser")
                    .with(jwt().jwt(j -> j.subject(userId.toString()))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.available").value(true));
        }
    }
}
