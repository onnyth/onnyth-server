package com.onnyth.onnythserver.exceptions;

import org.springframework.http.HttpStatus;

public class BadgeNotUnlockedException extends ApiException {

    public BadgeNotUnlockedException(String message) {
        super(message);
    }

    @Override
    public HttpStatus getHttpStatus() {
        return HttpStatus.BAD_REQUEST;
    }
}
