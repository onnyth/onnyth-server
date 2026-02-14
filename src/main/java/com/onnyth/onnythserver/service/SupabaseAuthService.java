package com.onnyth.onnythserver.service;

import com.onnyth.onnythserver.dto.AuthRequest;
import com.onnyth.onnythserver.dto.LoginResponse;
import com.onnyth.onnythserver.dto.RefreshTokenResponse;
import com.onnyth.onnythserver.dto.SignupResponse;
import com.onnyth.onnythserver.dto.supabase.SupabaseLoginResponse;
import com.onnyth.onnythserver.dto.supabase.SupabaseRefreshTokenResponse;
import com.onnyth.onnythserver.dto.supabase.SupabaseSignupResponse;
import com.onnyth.onnythserver.exceptions.*;
import com.onnyth.onnythserver.exceptions.handler.LogoutFailedException;
import com.onnyth.onnythserver.models.User;
import com.onnyth.onnythserver.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class SupabaseAuthService {

    private static final Logger logger = LoggerFactory.getLogger(SupabaseAuthService.class);

    private final RestTemplate restTemplate;
    private final UserRepository userRepository;

    @Value("${supabase.url}")
    private String supabaseUrl;

    @Value("${supabase.anon-key}")
    private String supabaseAnonKey;

    public SupabaseAuthService(RestTemplate restTemplate, UserRepository userRepository) {
        this.restTemplate = restTemplate;
        this.userRepository = userRepository;
    }

    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("apikey", supabaseAnonKey);
        return headers;
    }

    private HttpHeaders createHeadersWithAuth(String authorizationHeader) {
        HttpHeaders headers = createHeaders();
        headers.set(HttpHeaders.AUTHORIZATION, authorizationHeader);
        return headers;
    }

    /**
     * Signup flow:
     * 1. Register user with Supabase
     * 2. Return confirmation pending response
     * NOTE: User row is NOT created here - it's created on first login
     */
    public SignupResponse signUp(AuthRequest authRequest) {
        HttpEntity<AuthRequest> entity = new HttpEntity<>(authRequest, createHeaders());

        try {
            ResponseEntity<SupabaseSignupResponse> response = restTemplate.exchange(
                    supabaseUrl + "/auth/v1/signup",
                    HttpMethod.POST,
                    entity,
                    SupabaseSignupResponse.class
            );

            SupabaseSignupResponse supabaseResponse = response.getBody();
            if (supabaseResponse == null || supabaseResponse.id() == null) {
                throw new InvalidSignupRequestException("Empty response from Supabase");
            }

            logger.info("User registered with Supabase, id: {}", supabaseResponse.id());
            return SignupResponse.confirmationPending(supabaseResponse.email());

        } catch (HttpClientErrorException e) {
            String body = e.getResponseBodyAsString();
            logger.error("Supabase signup error response: {}", body);
            if (body.contains("User already registered")) {
                throw new EmailAlreadyExistsException(authRequest.email());
            }
            throw new InvalidSignupRequestException("Invalid signup request: " + body);
        } catch (HttpServerErrorException e) {
            throw new SupabaseUnavailableException();
        }
    }

    /**
     * Login flow:
     * 1. Authenticate with Supabase
     * 2. Check if user exists in local database
     * 3. If first login, create user with emailVerified=true
     * 4. Return tokens and user info
     */
    @Transactional
    public LoginResponse login(AuthRequest authRequest) {
        HttpEntity<AuthRequest> entity = new HttpEntity<>(authRequest, createHeaders());

        try {
            ResponseEntity<SupabaseLoginResponse> response = restTemplate.exchange(
                    supabaseUrl + "/auth/v1/token?grant_type=password",
                    HttpMethod.POST,
                    entity,
                    SupabaseLoginResponse.class
            );

            SupabaseLoginResponse supabaseResponse = response.getBody();
            if (supabaseResponse == null || supabaseResponse.supabaseUser() == null) {
                throw new InvalidSigninRequestException("Empty response from Supabase");
            }

            UUID userId = UUID.fromString(supabaseResponse.supabaseUser().id());
            String email = supabaseResponse.supabaseUser().email();
            logger.info("Supabase login successful for user ID: {}", userId);

            // Find or create user on first login
            User user = userRepository.findById(userId)
                    .orElseGet(() -> {
                        logger.info("First login - creating user in database with ID: {}", userId);
                        User newUser = User.builder()
                                .id(userId)
                                .email(email)
                                .emailVerified(true)
                                .build();
                        return userRepository.save(newUser);
                    });

            return new LoginResponse(
                    supabaseResponse.accessToken(),
                    supabaseResponse.refreshToken(),
                    supabaseResponse.expiresAt(),
                    new LoginResponse.UserInfo(
                            user.getId(),
                            user.getEmail(),
                            user.getUsername(),
                            user.getFullName(),
                            user.getProfilePic()
                    )
            );

        } catch (HttpClientErrorException e) {
            throw new InvalidSigninRequestException("Invalid email or password");
        } catch (HttpServerErrorException e) {
            throw new SupabaseUnavailableException();
        }
    }

    /**
     * Refresh token flow for mobile app:
     * 1. Exchange refresh token with Supabase
     * 2. Return new tokens with expiry timestamp
     */
    public RefreshTokenResponse refresh(String refreshToken) {
        HttpEntity<Map<String, String>> entity = new HttpEntity<>(
                Map.of("refresh_token", refreshToken),
                createHeaders()
        );

        try {
            ResponseEntity<SupabaseRefreshTokenResponse> response = restTemplate.exchange(
                    supabaseUrl + "/auth/v1/token?grant_type=refresh_token",
                    HttpMethod.POST,
                    entity,
                    SupabaseRefreshTokenResponse.class
            );

            SupabaseRefreshTokenResponse supabaseResponse = response.getBody();
            if (supabaseResponse == null) {
                throw new InvalidRefreshTokenException("Empty response from Supabase");
            }

            return new RefreshTokenResponse(
                    supabaseResponse.accessToken(),
                    supabaseResponse.refreshToken(),
                    supabaseResponse.expiresAt()
            );

        } catch (HttpClientErrorException e) {
            logger.error("Refresh token error: {}", e.getResponseBodyAsString());
            throw new InvalidRefreshTokenException("Invalid or expired refresh token");
        } catch (HttpServerErrorException e) {
            throw new SupabaseUnavailableException();
        }
    }

    /**
     * Logout - invalidate session on Supabase
     */
    public void logout(String authorizationHeader) {
        HttpEntity<Void> entity = new HttpEntity<>(createHeadersWithAuth(authorizationHeader));

        try {
            restTemplate.exchange(
                    supabaseUrl + "/auth/v1/logout",
                    HttpMethod.POST,
                    entity,
                    Void.class
            );
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            throw new LogoutFailedException("Logout failed");
        }
    }

}