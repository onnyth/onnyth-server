package com.onnyth.onnythserver.auth.application.exception;

import com.onnyth.onnythserver.shared.exception.ApiException;

import org.springframework.http.HttpStatus;

public class InvalidSigninRequestException extends ApiException {
    public InvalidSigninRequestException(String message) {
        super(message);
    }

    @Override
    public HttpStatus getHttpStatus() {
        return HttpStatus.BAD_REQUEST; // 400
    }
}
