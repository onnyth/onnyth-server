package com.onnyth.onnythserver.auth.application.exception;

import com.onnyth.onnythserver.shared.exception.ApiException;

import org.springframework.http.HttpStatus;

public class InvalidRefreshTokenException extends ApiException {
    public InvalidRefreshTokenException(String message) {
        super(message);
    }

    @Override
    public HttpStatus getHttpStatus() {
        return HttpStatus.BAD_REQUEST;
    }
}
