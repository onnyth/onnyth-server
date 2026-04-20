package com.onnyth.onnythserver.exceptions;

import org.springframework.http.HttpStatus;

public class BadgeNotFoundException extends ApiException {

    public BadgeNotFoundException(String message) {
        super(message);
    }

    @Override
    public HttpStatus getHttpStatus() {
        return HttpStatus.NOT_FOUND;
    }
}
