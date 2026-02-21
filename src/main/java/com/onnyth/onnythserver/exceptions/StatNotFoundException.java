package com.onnyth.onnythserver.exceptions;

import org.springframework.http.HttpStatus;

public class StatNotFoundException extends ApiException {

    public StatNotFoundException(String message) {
        super(message);
    }

    @Override
    public HttpStatus getHttpStatus() {
        return HttpStatus.NOT_FOUND;
    }
}
