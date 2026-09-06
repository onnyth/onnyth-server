package com.onnyth.onnythserver.user.application.exception;

import com.onnyth.onnythserver.shared.exception.ApiException;


import org.springframework.http.HttpStatus;

public class UsernameAlreadyExistsException extends ApiException {

    public UsernameAlreadyExistsException(String username) {
        super("Username already exists: " + username);
    }

    @Override
    public HttpStatus getHttpStatus() {
        return HttpStatus.CONFLICT;
    }
}

