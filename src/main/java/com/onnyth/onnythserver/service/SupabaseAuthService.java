package com.onnyth.onnythserver.service;

import com.onnyth.onnythserver.dto.AuthRequest;
import com.onnyth.onnythserver.dto.supabase.SupabaseLoginResponse;
import com.onnyth.onnythserver.dto.supabase.SupabaseSignupResponse;
import com.onnyth.onnythserver.exceptions.EmailAlreadyExistsException;
import com.onnyth.onnythserver.exceptions.InvalidSigninRequestException;
import com.onnyth.onnythserver.exceptions.InvalidSignupRequestException;
import com.onnyth.onnythserver.exceptions.SupabaseUnavailableException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.http.HttpHeaders;
import reactor.core.publisher.Mono;

@Service
public class SupabaseAuthService {

    private final WebClient webClient;

    @Value("${supabase.url}")
    private String supabaseUrl;

    @Value("${supabase.anon.key}")
    private String supabaseAnon;

    public SupabaseAuthService(WebClient webClient) {
        this.webClient = webClient;
    }

    public SupabaseSignupResponse signUp(AuthRequest authRequest) {
        return webClient.post()
                .uri(supabaseUrl + "/auth/v1/signup")
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .header("apikey", supabaseAnon)
                .bodyValue(authRequest)
                .retrieve().onStatus(
                        HttpStatusCode::is4xxClientError,
                        response -> response.bodyToMono(String.class)
                                .flatMap(body -> {
                                    if (body.contains("User already registered")) {
                                        return Mono.error(new EmailAlreadyExistsException(authRequest.email()));
                                    }
                                    return Mono.error(
                                            new InvalidSignupRequestException("Invalid signup request")
                                    );
                                })
                )
                .onStatus(
                        HttpStatusCode::is5xxServerError,
                        response -> Mono.error(new SupabaseUnavailableException())
                )
                .bodyToMono(SupabaseSignupResponse.class)
                .block();
    }

    public SupabaseLoginResponse login(AuthRequest authRequest) {
        return webClient.post()
                .uri(supabaseUrl + "/auth/v1/token?grant_type=password")
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .header("apikey", supabaseAnon)
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
                .block();
    }
}