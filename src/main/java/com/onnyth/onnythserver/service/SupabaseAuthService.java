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
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.http.HttpHeaders;
import reactor.core.publisher.Mono;

import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class SupabaseAuthService {

    private static final Logger logger = LoggerFactory.getLogger(SupabaseAuthService.class);

    private final WebClient webClient;

    @Value("${supabase.url}")
    private String supabaseUrl;

    @Value("${supabase.anon-key}")
    private String supabaseAnonKey;

    public SupabaseAuthService(WebClient webClient) {
        this.webClient = webClient;
    }

    public SignupResponse signUp(AuthRequest authRequest) {
        return webClient.post()
                .uri(supabaseUrl + "/auth/v1/signup")
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .header("apikey", supabaseAnonKey)
                .bodyValue(authRequest)
                .retrieve().onStatus(
                        HttpStatusCode::is4xxClientError,
                        response -> response.bodyToMono(String.class)
                                .flatMap(body -> {
                                    logger.error("Supabase signup error response: {}", body);
                                    if (body.contains("User already registered")) {
                                        return Mono.error(new EmailAlreadyExistsException(authRequest.email()));
                                    }
                                    return Mono.error(
                                            new InvalidSignupRequestException("Invalid signup request: " + body)
                                    );
                                })
                )
                .onStatus(
                        HttpStatusCode::is5xxServerError,
                        response -> Mono.error(new SupabaseUnavailableException())
                )
                .bodyToMono(SupabaseSignupResponse.class)
                .map(s -> new SignupResponse(
                        s.id(),
                        s.email(),
                        s.confirmationSentAt()
                ))
                .block();
    }

    public LoginResponse login(AuthRequest authRequest) {
        return webClient.post()
                .uri(supabaseUrl + "/auth/v1/token?grant_type=password")
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .header("apikey", supabaseAnonKey)
                .bodyValue(authRequest)
                .retrieve().onStatus(
                        HttpStatusCode::is4xxClientError,
                        response -> Mono.error(new InvalidSigninRequestException("Invalid Sign-in request"))
                )
                .onStatus(
                        HttpStatusCode::is5xxServerError,
                        response -> Mono.error(new SupabaseUnavailableException())
                )
                .bodyToMono(SupabaseLoginResponse.class)
                .map( s -> new LoginResponse(
                        s.accessToken(),
                        s.refreshToken(),
                        s.expiresIn(),
                        s.tokenType(),
                        s.expiresAt(),
                        s.supabaseUser()
                ))
                .block();
    }

    public RefreshTokenResponse refresh(String refreshToken) {
        return webClient.post()
                .uri(supabaseUrl + "/auth/v1/token?grant_type=refresh_token")
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .header("apikey", supabaseAnonKey)
                .bodyValue(Map.of("refresh_token", refreshToken))
                .retrieve()
                .onStatus(
                        HttpStatusCode::is4xxClientError,
                        r -> Mono.error(new InvalidRefreshTokenException("Invalid refresh token"))
                )
                .bodyToMono(SupabaseRefreshTokenResponse.class)
                .map(r -> new RefreshTokenResponse(
                        r.accessToken(),
                        r.refreshToken(),
                        r.expiresIn(),
                        r.tokenType()
                ))
                .block();
    }

    public void logout(String authorizationHeader) {
        webClient.post()
                .uri(supabaseUrl + "/auth/v1/logout")
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .header("apikey", supabaseAnonKey)
                .header(HttpHeaders.AUTHORIZATION, authorizationHeader)
                .retrieve()
                .onStatus(
                        HttpStatusCode::isError,
                        r -> Mono.error(new LogoutFailedException("Logout failed"))
                )
                .toBodilessEntity()
                .block();
    }

}