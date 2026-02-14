package com.onnyth.onnythserver.dto;

public record SignupResponse(
        String message,
        String email
) {
    public static SignupResponse confirmationPending(String email) {
        return new SignupResponse("Please check your email to confirm your account", email);
    }
}
