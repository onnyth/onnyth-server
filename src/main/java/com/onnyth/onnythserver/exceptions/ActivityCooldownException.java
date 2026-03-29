package com.onnyth.onnythserver.exceptions;

import org.springframework.http.HttpStatus;

public class ActivityCooldownException extends ApiException {

    public ActivityCooldownException(String message) {
        super(message);
    }

    @Override
    public HttpStatus getHttpStatus() {
        return HttpStatus.TOO_MANY_REQUESTS;
    }
}
