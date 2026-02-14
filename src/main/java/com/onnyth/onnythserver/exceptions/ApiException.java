package com.onnyth.onnythserver.exceptions;

import org.springframework.http.HttpStatus;

public abstract class ApiException extends RuntimeException {
    protected ApiException(String message) {
        super(message);
    }

    public abstract HttpStatus getHttpStatus();
}

