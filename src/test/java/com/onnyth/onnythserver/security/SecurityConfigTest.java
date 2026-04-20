package com.onnyth.onnythserver.security;

import com.onnyth.onnythserver.controller.AuthController;
import com.onnyth.onnythserver.controller.ProfileController;
import com.onnyth.onnythserver.controller.RegistrationController;
import com.onnyth.onnythserver.controller.UserController;
import com.onnyth.onnythserver.service.*;
import com.onnyth.onnythserver.support.MockJwtDecoderConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.not;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Verifies that the SecurityConfig correctly permits public routes
 * and protects all other routes behind JWT authentication.
 */
@WebMvcTest(controllers = { AuthController.class, ProfileController.class, UserController.class,
        RegistrationController.class })
@Import({ SecurityConfig.class, MockJwtDecoderConfig.class })
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SupabaseAuthService supabaseAuthService;

    @MockitoBean
    private ProfileService profileService;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private RankService rankService;

    @MockitoBean
    private RegistrationService registrationService;

    @MockitoBean
    private RegistrationCommitService registrationCommitService;

    @MockitoBean
    private StorageService storageService;

    // ─── Public routes — must be accessible without a token ──────────────────

    @Test
    @DisplayName("POST /api/v1/auth/signup is publicly accessible (not 401)")
    void signup_isPublic() throws Exception {
        mockMvc.perform(post("/api/v1/auth/signup")
                .contentType("application/json")
                .content("{\"email\":\"test@example.com\",\"password\":\"password123\"}"))
                .andExpect(status().is(not(401)));
    }

    @Test
    @DisplayName("POST /api/v1/auth/login is publicly accessible (not 401)")
    void login_isPublic() throws Exception {
        mockMvc.perform(post("/api/v1/auth/login")
                .contentType("application/json")
                .content("{\"email\":\"test@example.com\",\"password\":\"password123\"}"))
                .andExpect(status().is(not(401)));
    }

    @Test
    @DisplayName("POST /api/v1/auth/refresh is publicly accessible (not 401)")
    void refresh_isPublic() throws Exception {
        mockMvc.perform(post("/api/v1/auth/refresh")
                .contentType("application/json")
                .content("{\"refreshToken\":\"some-token\"}"))
                .andExpect(status().is(not(401)));
    }

    @Test
    @DisplayName("POST /api/v1/auth/logout is publicly accessible (not 401)")
    void logout_isPublic() throws Exception {
        mockMvc.perform(post("/api/v1/auth/logout"))
                .andExpect(status().is(not(401)));
    }

    // ─── Protected routes — must return 401 without a token ──────────────────

    @Test
    @DisplayName("GET /api/v1/profile returns 401 without JWT")
    void profile_requiresAuth() throws Exception {
        mockMvc.perform(get("/api/v1/profile"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("PUT /api/v1/profile returns 401 without JWT")
    void updateProfile_requiresAuth() throws Exception {
        mockMvc.perform(put("/api/v1/profile")
                .contentType("application/json")
                .content("{\"username\":\"newname\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /api/v1/profile/picture returns 401 without JWT")
    void uploadPicture_requiresAuth() throws Exception {
        mockMvc.perform(post("/api/v1/profile/picture"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /api/users returns 401 without JWT")
    void users_requiresAuth() throws Exception {
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isUnauthorized());
    }

    // ─── Protected routes — must pass with a valid JWT ────────────────────────

    @Test
    @DisplayName("GET /api/v1/profile passes security with valid JWT (not 401/403)")
    void profile_passesWithJwt() throws Exception {
        mockMvc.perform(get("/api/v1/profile")
                .with(jwt().jwt(j -> j.subject("00000000-0000-0000-0000-000000000001"))))
                .andExpect(status().is(not(401)))
                .andExpect(status().is(not(403)));
    }

    @Test
    @DisplayName("GET /api/users passes security with valid JWT (not 401/403)")
    void users_passesWithJwt() throws Exception {
        mockMvc.perform(get("/api/users")
                .with(jwt()))
                .andExpect(status().is(not(401)))
                .andExpect(status().is(not(403)));
    }

    // ─── Profile Card routes ──────────────────────────────────────────────────

    @Test
    @DisplayName("GET /api/v1/users/{userId}/card is publicly accessible (not 401)")
    void publicProfileCard_isPublic() throws Exception {
        mockMvc.perform(get("/api/v1/users/00000000-0000-0000-0000-000000000001/card"))
                .andExpect(status().is(not(401)));
    }

    @Test
    @DisplayName("GET /api/v1/profile/card returns 401 without JWT")
    void authenticatedProfileCard_requiresAuth() throws Exception {
        mockMvc.perform(get("/api/v1/profile/card"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /api/v1/profile/card passes security with valid JWT (not 401/403)")
    void authenticatedProfileCard_passesWithJwt() throws Exception {
        mockMvc.perform(get("/api/v1/profile/card")
                .with(jwt().jwt(j -> j.subject("00000000-0000-0000-0000-000000000001"))))
                .andExpect(status().is(not(401)))
                .andExpect(status().is(not(403)));
    }

    // ─── Registration routes — all require JWT ───────────────────────────────

    @Test
    @DisplayName("GET /api/v1/registration/status returns 401 without JWT")
    void registrationStatus_requiresAuth() throws Exception {
        mockMvc.perform(get("/api/v1/registration/status"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("PUT /api/v1/registration/step/PHONE returns 401 without JWT")
    void registrationStep_requiresAuth() throws Exception {
        mockMvc.perform(put("/api/v1/registration/step/PHONE")
                .contentType("application/json")
                .content("{\"phone\":\"+1234567890\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /api/v1/registration/complete returns 401 without JWT")
    void registrationComplete_requiresAuth() throws Exception {
        mockMvc.perform(post("/api/v1/registration/complete"))
                .andExpect(status().isUnauthorized());
    }

    // ─── Rank routes ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("GET /api/v1/profile/rank returns 401 without JWT")
    void rankProgress_requiresAuth() throws Exception {
        mockMvc.perform(get("/api/v1/profile/rank"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /api/v1/profile/rank passes security with valid JWT (not 401/403)")
    void rankProgress_passesWithJwt() throws Exception {
        mockMvc.perform(get("/api/v1/profile/rank")
                .with(jwt().jwt(j -> j.subject("00000000-0000-0000-0000-000000000001"))))
                .andExpect(status().is(not(401)))
                .andExpect(status().is(not(403)));
    }
}
