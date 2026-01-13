package com.onnyth.onnythserver.dto;

import java.time.Instant;

public record ApiErrorResponse(
        int status,
        String error,
        String message,
        String path,
        Instant timestamp
) {}