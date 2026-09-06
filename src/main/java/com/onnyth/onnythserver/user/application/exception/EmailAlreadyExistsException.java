package com.onnyth.onnythserver.user.application.exception;

import com.onnyth.onnythserver.shared.exception.ApiException;


import org.springframework.http.HttpStatus;

public class EmailAlreadyExistsException extends ApiException {
    public EmailAlreadyExistsException(String email) {
        super("Email already exists: " + email);
    }

    @Override
    public HttpStatus getHttpStatus() {
        return HttpStatus.CONFLICT;
    }
}
