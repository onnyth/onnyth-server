package com.onnyth.onnythserver.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.onnyth.onnythserver.dto.AuthRequest;
import com.onnyth.onnythserver.dto.LoginResponse;
import com.onnyth.onnythserver.dto.RefreshTokenRequest;
import com.onnyth.onnythserver.dto.RefreshTokenResponse;
import com.onnyth.onnythserver.dto.SignupResponse;
import com.onnyth.onnythserver.exceptions.EmailAlreadyExistsException;
import com.onnyth.onnythserver.exceptions.InvalidSigninRequestException;
import com.onnyth.onnythserver.exceptions.InvalidSignupRequestException;
import com.onnyth.onnythserver.exceptions.SupabaseUnavailableException;
import com.onnyth.onnythserver.exceptions.handler.LogoutFailedException;
import com.onnyth.onnythserver.service.SupabaseAuthService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Web layer tests for AuthController.
 * Tests HTTP routing, request/response serialization, and error handling.
 * Supabase service is mocked — no real auth calls are made.
 */
@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        @MockitoBean
        private SupabaseAuthService supabaseAuthService;

        // ─── POST /api/v1/auth/signup ─────────────────────────────────────────────

        @Nested
        @DisplayName("POST /api/v1/auth/signup")
        class Signup {

                @Test
                @DisplayName("returns 201 with confirmation-pending response on success")
                void returns201_onSuccess() throws Exception {
                        SignupResponse response = SignupResponse.confirmationPending("new@example.com");
                        when(supabaseAuthService.signUp(any(AuthRequest.class))).thenReturn(response);

                        mockMvc.perform(post("/api/v1/auth/signup")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(
                                                        new AuthRequest("new@example.com", "password123"))))
                                        .andExpect(status().isCreated())
                                        .andExpect(jsonPath("$.email").value("new@example.com"));
                }

        @Test
        @DisplayName("returns 409 when email already exists")
        void returns409_whenEmailAlreadyExists() throws Exception {
            when(supabaseAuthService.signUp(any(AuthRequest.class)))
                    .thenThrow(new EmailAlreadyExistsException("existing@example.com"));

            mockMvc.perform(post("/api/v1/auth/signup")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(new AuthRequest("existing@example.com", "password123"))))
                    .andExpect(status().isConflict());
        }

        @Test
        @DisplayName("returns 400 on invalid signup request")
        void returns400_onInvalidRequest() throws Exception {
            when(supabaseAuthService.signUp(any(AuthRequest.class)))
                    .thenThrow(new InvalidSignupRequestException("Invalid request"));

            mockMvc.perform(post("/api/v1/auth/signup")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(new AuthRequest("bad", "pw"))))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("returns 502 when Supabase is unavailable (bad gateway)")
        void returns503_whenSupabaseUnavailable() throws Exception {
            when(supabaseAuthService.signUp(any(AuthRequest.class)))
                    .thenThrow(new SupabaseUnavailableException());

            mockMvc.perform(post("/api/v1/auth/signup")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(new AuthRequest("test@example.com", "password123"))))
                    .andExpect(status().isBadGateway()); // SupabaseUnavailableException → 502
        }
        }

        // ─── POST /api/v1/auth/login ──────────────────────────────────────────────

        @Nested
        @DisplayName("POST /api/v1/auth/login")
        class Login {

                @Test
                @DisplayName("returns 200 with tokens and user info on success")
                void returns200_onSuccess() throws Exception {
                        LoginResponse.UserInfo userInfo = new LoginResponse.UserInfo(
                                        UUID.randomUUID(), "user@example.com", "username", "Full Name", null, true);
                        LoginResponse response = new LoginResponse("access-token", "refresh-token", 9999999L, userInfo);

                        when(supabaseAuthService.login(any(AuthRequest.class))).thenReturn(response);

                        mockMvc.perform(post("/api/v1/auth/login")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(
                                                        new AuthRequest("user@example.com", "password123"))))
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath("$.accessToken").value("access-token"))
                                        .andExpect(jsonPath("$.refreshToken").value("refresh-token"))
                                        .andExpect(jsonPath("$.user.email").value("user@example.com"));
                }

        @Test
        @DisplayName("returns 400 on invalid credentials (bad request)")
        void returns401_onInvalidCredentials() throws Exception {
            when(supabaseAuthService.login(any(AuthRequest.class)))
                    .thenThrow(new InvalidSigninRequestException("Invalid email or password"));

            mockMvc.perform(post("/api/v1/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(new AuthRequest("bad@example.com", "wrong"))))
                    .andExpect(status().isBadRequest()); // InvalidSigninRequestException → 400
        }
        }

        // ─── POST /api/v1/auth/refresh ────────────────────────────────────────────

        @Nested
        @DisplayName("POST /api/v1/auth/refresh")
        class Refresh {

                @Test
                @DisplayName("returns 200 with new tokens on success")
                void returns200_onSuccess() throws Exception {
                        RefreshTokenResponse response = new RefreshTokenResponse("new-access", "new-refresh", 9999999L);
                        when(supabaseAuthService.refresh(anyString())).thenReturn(response);

                        mockMvc.perform(post("/api/v1/auth/refresh")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(
                                                        new RefreshTokenRequest("valid-refresh-token"))))
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath("$.accessToken").value("new-access"));
                }
        }

        // ─── POST /api/v1/auth/logout ─────────────────────────────────────────────

        @Nested
        @DisplayName("POST /api/v1/auth/logout")
        class Logout {

                @Test
                @DisplayName("returns 204 on successful logout")
                void returns204_onSuccess() throws Exception {
                        doNothing().when(supabaseAuthService).logout(anyString());

                        mockMvc.perform(post("/api/v1/auth/logout")
                                        .header("Authorization", "Bearer valid-token"))
                                        .andExpect(status().isNoContent());
                }

                @Test
                @DisplayName("returns 500 when logout fails")
                void returns500_whenLogoutFails() throws Exception {
                        doThrow(new LogoutFailedException("Logout failed"))
                                        .when(supabaseAuthService).logout(anyString());

                        mockMvc.perform(post("/api/v1/auth/logout")
                                        .header("Authorization", "Bearer bad-token"))
                                        .andExpect(status().isInternalServerError());
                }
        }
}
