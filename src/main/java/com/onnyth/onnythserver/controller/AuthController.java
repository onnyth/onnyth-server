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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


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
    public ResponseEntity<RefreshTokenResponse> refresh(@RequestBody RefreshTokenRequest request) {
        return ResponseEntity.ok(supabaseAuthService.refresh(request.refreshToken()));
    }

}
