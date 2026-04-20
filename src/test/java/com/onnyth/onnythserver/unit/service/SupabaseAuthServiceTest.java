package com.onnyth.onnythserver.unit.service;

import com.onnyth.onnythserver.dto.AuthRequest;
import com.onnyth.onnythserver.dto.LoginResponse;
import com.onnyth.onnythserver.dto.RefreshTokenResponse;
import com.onnyth.onnythserver.dto.SignupResponse;
import com.onnyth.onnythserver.dto.supabase.SupabaseLoginResponse;
import com.onnyth.onnythserver.dto.supabase.SupabaseRefreshTokenResponse;
import com.onnyth.onnythserver.dto.supabase.SupabaseSignupResponse;
import com.onnyth.onnythserver.dto.supabase.SupabaseUser;
import com.onnyth.onnythserver.exceptions.EmailAlreadyExistsException;
import com.onnyth.onnythserver.exceptions.InvalidRefreshTokenException;
import com.onnyth.onnythserver.exceptions.InvalidSigninRequestException;
import com.onnyth.onnythserver.exceptions.InvalidSignupRequestException;
import com.onnyth.onnythserver.exceptions.SupabaseUnavailableException;
import com.onnyth.onnythserver.exceptions.handler.LogoutFailedException;
import com.onnyth.onnythserver.models.User;
import com.onnyth.onnythserver.repository.UserRepository;
import com.onnyth.onnythserver.service.SupabaseAuthService;
import com.onnyth.onnythserver.support.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for SupabaseAuthService — mocks RestTemplate and UserRepository
 * to test all auth flows in isolation.
 */
@ExtendWith(MockitoExtension.class)
class SupabaseAuthServiceTest {

        @Mock
        private RestTemplate restTemplate;

        @Mock
        private UserRepository userRepository;

        private SupabaseAuthService supabaseAuthService;

        private static final String SUPABASE_URL = "https://test.supabase.co";
        private static final String ANON_KEY = "test-anon-key";

        @BeforeEach
        void setUp() {
                supabaseAuthService = new SupabaseAuthService(restTemplate, userRepository);
                ReflectionTestUtils.setField(supabaseAuthService, "supabaseUrl", SUPABASE_URL);
                ReflectionTestUtils.setField(supabaseAuthService, "supabaseAnonKey", ANON_KEY);
        }

        // ─── signUp() ─────────────────────────────────────────────────────────────

        @Nested
        @DisplayName("signUp()")
        class SignUp {

                @Test
                @DisplayName("returns confirmation-pending response on successful signup")
                void returnsConfirmationPending_onSuccess() {
                        AuthRequest request = new AuthRequest("new@example.com", "password123");
                        SupabaseSignupResponse supabaseResponse = new SupabaseSignupResponse(
                                        UUID.randomUUID().toString(), "new@example.com", null);

                        when(restTemplate.exchange(anyString(), any(), any(), eq(SupabaseSignupResponse.class)))
                                        .thenReturn(ResponseEntity.ok(supabaseResponse));

                        SignupResponse result = supabaseAuthService.signUp(request);

                        assertThat(result).isNotNull();
                        assertThat(result.email()).isEqualTo("new@example.com");
                }

                @Test
                @DisplayName("throws EmailAlreadyExistsException when Supabase returns 'User already registered'")
                void throwsEmailAlreadyExists_whenUserAlreadyRegistered() {
                        AuthRequest request = new AuthRequest("existing@example.com", "password123");

                        HttpClientErrorException ex = HttpClientErrorException.create(
                                        HttpStatus.UNPROCESSABLE_ENTITY, "Unprocessable Entity",
                                        null, "{\"msg\":\"User already registered\"}".getBytes(), null);

                        when(restTemplate.exchange(anyString(), any(), any(), eq(SupabaseSignupResponse.class)))
                                        .thenThrow(ex);

                        assertThatThrownBy(() -> supabaseAuthService.signUp(request))
                                        .isInstanceOf(EmailAlreadyExistsException.class);
                }

                @Test
                @DisplayName("throws InvalidSignupRequestException on other 4xx errors")
                void throwsInvalidSignupRequest_onOther4xx() {
                        AuthRequest request = new AuthRequest("bad@example.com", "pw");

                        HttpClientErrorException ex = HttpClientErrorException.create(
                                        HttpStatus.BAD_REQUEST, "Bad Request",
                                        null, "{\"msg\":\"Invalid email\"}".getBytes(), null);

                        when(restTemplate.exchange(anyString(), any(), any(), eq(SupabaseSignupResponse.class)))
                                        .thenThrow(ex);

                        assertThatThrownBy(() -> supabaseAuthService.signUp(request))
                                        .isInstanceOf(InvalidSignupRequestException.class);
                }

                @Test
                @DisplayName("throws SupabaseUnavailableException on 5xx errors")
                void throwsSupabaseUnavailable_on5xx() {
                        AuthRequest request = new AuthRequest("test@example.com", "password123");

                        when(restTemplate.exchange(anyString(), any(), any(), eq(SupabaseSignupResponse.class)))
                                        .thenThrow(new HttpServerErrorException(HttpStatus.SERVICE_UNAVAILABLE));

                        assertThatThrownBy(() -> supabaseAuthService.signUp(request))
                                        .isInstanceOf(SupabaseUnavailableException.class);
                }
        }

        // ─── login() ──────────────────────────────────────────────────────────────

        @Nested
        @DisplayName("login()")
        class Login {

                private UUID userId;
                private String email;
                private SupabaseLoginResponse supabaseLoginResponse;

                @BeforeEach
                void setUp() {
                        userId = UUID.randomUUID();
                        email = "user@example.com";
                        SupabaseUser supabaseUser = new SupabaseUser(userId.toString(), email);
                        supabaseLoginResponse = new SupabaseLoginResponse(
                                        "access-token-123", "refresh-token-456", 3600, "bearer", 9999999L,
                                        supabaseUser);
                }

        @Test
        @DisplayName("creates new user in DB on first login")
        void createsNewUser_onFirstLogin() {
            when(restTemplate.exchange(anyString(), any(), any(), eq(SupabaseLoginResponse.class)))
                    .thenReturn(ResponseEntity.ok(supabaseLoginResponse));
            when(userRepository.findById(userId)).thenReturn(Optional.empty());
            when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

            LoginResponse result = supabaseAuthService.login(new AuthRequest(email, "password123"));

            assertThat(result.accessToken()).isEqualTo("access-token-123");
            assertThat(result.refreshToken()).isEqualTo("refresh-token-456");
            assertThat(result.user().id()).isEqualTo(userId);
            assertThat(result.user().email()).isEqualTo(email);
            verify(userRepository).save(any(User.class));
        }

                @Test
                @DisplayName("returns existing user from DB on subsequent logins")
                void returnsExistingUser_onSubsequentLogin() {
                        User existingUser = TestDataFactory.aUser()
                                        .id(userId)
                                        .email(email)
                                        .username("existinguser")
                                        .build();

                        when(restTemplate.exchange(anyString(), any(), any(), eq(SupabaseLoginResponse.class)))
                                        .thenReturn(ResponseEntity.ok(supabaseLoginResponse));
                        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));

                        LoginResponse result = supabaseAuthService.login(new AuthRequest(email, "password123"));

                        assertThat(result.user().username()).isEqualTo("existinguser");
                        verify(userRepository, never()).save(any(User.class));
                }

        @Test
        @DisplayName("new user is created with emailVerified=true")
        void newUser_hasEmailVerifiedTrue() {
            when(restTemplate.exchange(anyString(), any(), any(), eq(SupabaseLoginResponse.class)))
                    .thenReturn(ResponseEntity.ok(supabaseLoginResponse));
            when(userRepository.findById(userId)).thenReturn(Optional.empty());
            when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

            supabaseAuthService.login(new AuthRequest(email, "password123"));

            verify(userRepository).save(argThat(user -> Boolean.TRUE.equals(user.getEmailVerified())));
        }

        @Test
        @DisplayName("throws InvalidSigninRequestException on invalid credentials")
        void throwsInvalidSignin_onBadCredentials() {
            when(restTemplate.exchange(anyString(), any(), any(), eq(SupabaseLoginResponse.class)))
                    .thenThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST));

            assertThatThrownBy(() -> supabaseAuthService.login(new AuthRequest("bad@example.com", "wrong")))
                    .isInstanceOf(InvalidSigninRequestException.class);
        }

        @Test
        @DisplayName("throws SupabaseUnavailableException on 5xx errors")
        void throwsSupabaseUnavailable_on5xx() {
            when(restTemplate.exchange(anyString(), any(), any(), eq(SupabaseLoginResponse.class)))
                    .thenThrow(new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR));

            assertThatThrownBy(() -> supabaseAuthService.login(new AuthRequest("test@example.com", "pw")))
                    .isInstanceOf(SupabaseUnavailableException.class);
        }
        }

        // ─── refresh() ────────────────────────────────────────────────────────────

        @Nested
        @DisplayName("refresh()")
        class Refresh {

                @Test
                @DisplayName("returns new tokens on valid refresh token")
                void returnsNewTokens_onValidRefreshToken() {
                        SupabaseRefreshTokenResponse supabaseResponse = new SupabaseRefreshTokenResponse(
                                        "new-access-token", "new-refresh-token", 3600, 9999999L, "bearer");

                        when(restTemplate.exchange(anyString(), any(), any(), eq(SupabaseRefreshTokenResponse.class)))
                                        .thenReturn(ResponseEntity.ok(supabaseResponse));

                        RefreshTokenResponse result = supabaseAuthService.refresh("valid-refresh-token");

                        assertThat(result.accessToken()).isEqualTo("new-access-token");
                        assertThat(result.refreshToken()).isEqualTo("new-refresh-token");
                }

        @Test
        @DisplayName("throws InvalidRefreshTokenException on invalid/expired token")
        void throwsInvalidRefreshToken_onExpiredToken() {
            when(restTemplate.exchange(anyString(), any(), any(), eq(SupabaseRefreshTokenResponse.class)))
                    .thenThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST));

            assertThatThrownBy(() -> supabaseAuthService.refresh("expired-token"))
                    .isInstanceOf(InvalidRefreshTokenException.class);
        }

        @Test
        @DisplayName("throws SupabaseUnavailableException on 5xx errors")
        void throwsSupabaseUnavailable_on5xx() {
            when(restTemplate.exchange(anyString(), any(), any(), eq(SupabaseRefreshTokenResponse.class)))
                    .thenThrow(new HttpServerErrorException(HttpStatus.SERVICE_UNAVAILABLE));

            assertThatThrownBy(() -> supabaseAuthService.refresh("some-token"))
                    .isInstanceOf(SupabaseUnavailableException.class);
        }
        }

        // ─── logout() ─────────────────────────────────────────────────────────────

        @Nested
        @DisplayName("logout()")
        class Logout {

        @Test
        @DisplayName("completes successfully when Supabase returns 2xx")
        void completesSuccessfully() {
            when(restTemplate.exchange(anyString(), any(), any(), eq(Void.class)))
                    .thenReturn(ResponseEntity.noContent().build());

            // Should not throw
            supabaseAuthService.logout("Bearer valid-token");
        }

        @Test
        @DisplayName("throws LogoutFailedException on 4xx error")
        void throwsLogoutFailed_on4xx() {
            when(restTemplate.exchange(anyString(), any(), any(), eq(Void.class)))
                    .thenThrow(new HttpClientErrorException(HttpStatus.UNAUTHORIZED));

            assertThatThrownBy(() -> supabaseAuthService.logout("Bearer bad-token"))
                    .isInstanceOf(LogoutFailedException.class);
        }

        @Test
        @DisplayName("throws LogoutFailedException on 5xx error")
        void throwsLogoutFailed_on5xx() {
            when(restTemplate.exchange(anyString(), any(), any(), eq(Void.class)))
                    .thenThrow(new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR));

            assertThatThrownBy(() -> supabaseAuthService.logout("Bearer token"))
                    .isInstanceOf(LogoutFailedException.class);
        }
        }
}
