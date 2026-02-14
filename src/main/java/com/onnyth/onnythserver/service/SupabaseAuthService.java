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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class SupabaseAuthService {

    private static final Logger logger = LoggerFactory.getLogger(SupabaseAuthService.class);

    private final RestTemplate restTemplate;

    @Value("${supabase.url}")
    private String supabaseUrl;

    @Value("${supabase.anon-key}")
    private String supabaseAnonKey;

    public SupabaseAuthService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
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

    public SignupResponse signUp(AuthRequest authRequest) {
        HttpEntity<AuthRequest> entity = new HttpEntity<>(authRequest, createHeaders());

        try {
            ResponseEntity<SupabaseSignupResponse> response = restTemplate.exchange(
                    supabaseUrl + "/auth/v1/signup",
                    HttpMethod.POST,
                    entity,
                    SupabaseSignupResponse.class
            );

            SupabaseSignupResponse s = response.getBody();
            if (s == null) {
                throw new InvalidSignupRequestException("Empty response from Supabase");
            }

            return new SignupResponse(
                    s.id(),
                    s.email(),
                    s.confirmationSentAt()
            );
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

    public LoginResponse login(AuthRequest authRequest) {
        HttpEntity<AuthRequest> entity = new HttpEntity<>(authRequest, createHeaders());

        try {
            ResponseEntity<SupabaseLoginResponse> response = restTemplate.exchange(
                    supabaseUrl + "/auth/v1/token?grant_type=password",
                    HttpMethod.POST,
                    entity,
                    SupabaseLoginResponse.class
            );

            SupabaseLoginResponse s = response.getBody();
            if (s == null) {
                throw new InvalidSigninRequestException("Empty response from Supabase");
            }

            return new LoginResponse(
                    s.accessToken(),
                    s.refreshToken(),
                    s.expiresIn(),
                    s.tokenType(),
                    s.expiresAt(),
                    s.supabaseUser()
            );
        } catch (HttpClientErrorException e) {
            throw new InvalidSigninRequestException("Invalid Sign-in request");
        } catch (HttpServerErrorException e) {
            throw new SupabaseUnavailableException();
        }
    }

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

            SupabaseRefreshTokenResponse r = response.getBody();
            if (r == null) {
                throw new InvalidRefreshTokenException("Empty response from Supabase");
            }

            return new RefreshTokenResponse(
                    r.accessToken(),
                    r.refreshToken(),
                    r.expiresIn(),
                    r.tokenType()
            );
        } catch (HttpClientErrorException e) {
            throw new InvalidRefreshTokenException("Invalid refresh token");
        }
    }

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