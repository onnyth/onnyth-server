package com.onnyth.onnythserver.controller;

import com.onnyth.onnythserver.dto.AuthRequest;
import com.onnyth.onnythserver.dto.AuthResponse;
import com.onnyth.onnythserver.dto.SignupResponse;
import com.onnyth.onnythserver.service.SupabaseAuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final SupabaseAuthService supabaseAuthService;

    public SignupResponse signUp(AuthRequest authRequest) {
        return supabaseAuthService.signUp(authRequest);
    }

    public AuthResponse signIn(AuthRequest authRequest) {
        return supabaseAuthService.signIn(authRequest);
    }

}
