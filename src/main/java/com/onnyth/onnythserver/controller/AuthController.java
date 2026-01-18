package com.onnyth.onnythserver.controller;

import com.onnyth.onnythserver.dto.AuthRequest;
import com.onnyth.onnythserver.dto.RefreshTokenRequest;
import com.onnyth.onnythserver.dto.RefreshTokenResponse;
import com.onnyth.onnythserver.dto.supabase.SupabaseLoginResponse;
import com.onnyth.onnythserver.dto.supabase.SupabaseSignupResponse;
import com.onnyth.onnythserver.service.SupabaseAuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final SupabaseAuthService supabaseAuthService;

    @PostMapping("/signup")
    public ResponseEntity<SupabaseSignupResponse> signUp(@RequestBody AuthRequest authRequest) {
        return ResponseEntity.status(HttpStatus.CREATED).body(supabaseAuthService.signUp(authRequest));
    }

    @PostMapping("/login")
    public ResponseEntity<SupabaseLoginResponse> login(@RequestBody AuthRequest authRequest) {
        SupabaseLoginResponse supabaseLoginResponse = supabaseAuthService.login(authRequest);
        return ResponseEntity.status(HttpStatus.OK).body(supabaseLoginResponse);
    }

    @PostMapping("/refresh")
    public ResponseEntity<RefreshTokenResponse> refresh(@RequestBody RefreshTokenRequest refreshTokenRequest) {
        return ResponseEntity.ok(supabaseAuthService.refresh(refreshTokenRequest.refreshToken()));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestHeader("Authorization") String authorization) {
        supabaseAuthService.logout(authorization);
        return ResponseEntity.noContent().build();
    }

}
