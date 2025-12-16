package com.onnyth.onnythserver.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.onnyth.onnythserver.dto.AuthRequest;
import com.onnyth.onnythserver.dto.AuthResponse;
import com.onnyth.onnythserver.dto.SignupResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

@Service
public class SupabaseAuthService {
    @Value("supabase.url")
    private String supabaseUrl;

    @Value("supabase.anon.key")
    private String supabaseAnon;

    public SignupResponse signUp(AuthRequest authRequest) {
        WebClient client = WebClient.builder()
                .baseUrl(supabaseUrl)
                .defaultHeader("apikey", supabaseAnon)
                .defaultHeader("Authorization", "Bearer " + supabaseAnon)
                .build();

        Map<String, Object> body = Map.of(
                "email", authRequest.email(),
                "password", authRequest.password()
        );

        return client.post()
                .uri("/auth/v1/signup")
                .bodyValue(body)
                .retrieve()
                .bodyToMono(JsonNode.class)
                .map(json -> new SignupResponse(true))
                .block();
    }

    public AuthResponse signIn(AuthRequest authRequest) {
        WebClient client = WebClient.builder()
                .baseUrl(supabaseUrl)
                .defaultHeader("apikey", supabaseAnon)
                .defaultHeader("Authorization", "Bearer " + supabaseAnon)
                .build();

        Map<String, Object> body = Map.of(
                "email", authRequest.email(),
                "password", authRequest.password()
        );

        return client.post()
                .uri("/auth/v1/token?grant_type=password")
                .bodyValue(body)
                .retrieve()
                .bodyToMono(JsonNode.class)
                .map(json -> new AuthResponse(
                        json.get("access_token").asText(),
                        json.get("refresh_token").asText()
                ))
                .block();
    }
}
