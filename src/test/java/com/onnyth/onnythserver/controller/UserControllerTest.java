package com.onnyth.onnythserver.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.onnyth.onnythserver.dto.ProfileCardResponse;
import com.onnyth.onnythserver.exceptions.UserNotFoundException;
import com.onnyth.onnythserver.models.User;
import com.onnyth.onnythserver.service.ProfileService;
import com.onnyth.onnythserver.service.UserService;
import com.onnyth.onnythserver.support.TestDataFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.onnyth.onnythserver.security.SecurityConfig;
import com.onnyth.onnythserver.support.MockJwtDecoderConfig;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Web layer tests for UserController.
 */
@WebMvcTest(UserController.class)
@Import({ SecurityConfig.class, MockJwtDecoderConfig.class })
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private ProfileService profileService;

    private static final UUID USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");

    @Nested
    @DisplayName("GET /api/users")
    class GetAllUsers {

        @Test
        @DisplayName("returns 200 with list of users when authenticated")
        void returns200_whenAuthenticated() throws Exception {
            User user = TestDataFactory.aUserWithId(USER_ID);
            when(userService.getAllUsers()).thenReturn(List.of(user));

            mockMvc.perform(get("/api/users").with(jwt()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$.length()").value(1));
        }

        @Test
        @DisplayName("returns 401 when not authenticated")
        void returns401_whenNotAuthenticated() throws Exception {
            mockMvc.perform(get("/api/users"))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("GET /api/users/{id}")
    class GetUserById {

        @Test
        @DisplayName("returns 200 with user when found")
        void returns200_whenFound() throws Exception {
            User user = TestDataFactory.aUserWithId(USER_ID);
            when(userService.getUserById(USER_ID)).thenReturn(Optional.of(user));

            mockMvc.perform(get("/api/users/" + USER_ID).with(jwt()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(USER_ID.toString()));
        }

        @Test
        @DisplayName("returns 404 when user not found")
        void returns404_whenNotFound() throws Exception {
            when(userService.getUserById(USER_ID)).thenReturn(Optional.empty());

            mockMvc.perform(get("/api/users/" + USER_ID).with(jwt()))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("GET /api/users/email/{email}")
    class GetUserByEmail {

        @Test
        @DisplayName("returns 200 with user when email found")
        void returns200_whenFound() throws Exception {
            User user = TestDataFactory.aUserWithEmail("test@example.com");
            when(userService.getUserByEmail("test@example.com")).thenReturn(Optional.of(user));

            mockMvc.perform(get("/api/users/email/test@example.com").with(jwt()))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("returns 404 when email not found")
        void returns404_whenNotFound() throws Exception {
            when(userService.getUserByEmail("unknown@example.com")).thenReturn(Optional.empty());

            mockMvc.perform(get("/api/users/email/unknown@example.com").with(jwt()))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("POST /api/users")
    class CreateUser {

        @Test
        @DisplayName("returns 200 with created user")
        void returns200_onSuccess() throws Exception {
            User user = TestDataFactory.aUserWithId(USER_ID);
            when(userService.createUser(any(User.class))).thenReturn(user);

            mockMvc.perform(post("/api/users")
                    .with(jwt())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(user)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(USER_ID.toString()));
        }
    }

    @Nested
    @DisplayName("DELETE /api/users/{id}")
    class DeleteUser {

        @Test
        @DisplayName("returns 204 on successful deletion")
        void returns204_onSuccess() throws Exception {
            doNothing().when(userService).deleteUser(USER_ID);

            mockMvc.perform(delete("/api/users/" + USER_ID).with(jwt()))
                    .andExpect(status().isNoContent());
        }
    }

    // ─── GET /api/v1/users/{userId}/card (public endpoint) ────────────────────

    @Nested
    @DisplayName("GET /api/v1/users/{userId}/card")
    class GetUserProfileCard {

        @Test
        @DisplayName("returns 200 with profile card (no auth required)")
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

            // No JWT required — this is a public endpoint
            mockMvc.perform(get("/api/v1/users/" + USER_ID + "/card"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.userId").value(USER_ID.toString()))
                    .andExpect(jsonPath("$.username").value("hero"))
                    .andExpect(jsonPath("$.rankTier").value("Novice"))
                    .andExpect(jsonPath("$.totalScore").value(0));
        }

        @Test
        @DisplayName("returns 404 when user not found")
        void returns404_whenUserNotFound() throws Exception {
            when(profileService.getProfileCard(USER_ID))
                    .thenThrow(new UserNotFoundException(USER_ID.toString()));

            mockMvc.perform(get("/api/v1/users/" + USER_ID + "/card"))
                    .andExpect(status().isNotFound());
        }
    }
}
