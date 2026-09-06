package com.onnyth.onnythserver.auth.adapter.in.rest;

import com.onnyth.onnythserver.auth.adapter.in.rest.dto.*;
import com.onnyth.onnythserver.auth.application.usecase.SupabaseAuthUseCaseService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final SupabaseAuthUseCaseService supabaseAuthService;

    @PostMapping("/signup")
    public ResponseEntity<SignupResponse> signUp(@RequestBody AuthRequest authRequest) {
        return ResponseEntity.status(HttpStatus.CREATED).body(supabaseAuthService.signUp(authRequest));
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody AuthRequest authRequest) {
        return ResponseEntity.status(HttpStatus.OK).body(supabaseAuthService.login(authRequest));
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
