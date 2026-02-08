package com.onnyth.onnythserver.dto;

import java.time.Instant;

public record SignupResponse(
        String id,
        String email,
        String confirmationSentAt
) {}
