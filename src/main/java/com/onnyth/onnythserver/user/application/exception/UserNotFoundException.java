package com.onnyth.onnythserver.user.application.exception;

import com.onnyth.onnythserver.shared.exception.ApiException;


import org.springframework.http.HttpStatus;

public class UserNotFoundException extends ApiException {

    public UserNotFoundException(String userId) {
        super("User not found: " + userId);
    }

    @Override
    public HttpStatus getHttpStatus() {
        return HttpStatus.NOT_FOUND;
    }
}

