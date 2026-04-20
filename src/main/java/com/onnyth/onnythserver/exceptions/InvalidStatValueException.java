package com.onnyth.onnythserver.exceptions;

import org.springframework.http.HttpStatus;

public class InvalidStatValueException extends ApiException {

    public InvalidStatValueException(String message) {
        super(message);
    }

    @Override
    public HttpStatus getHttpStatus() {
        return HttpStatus.BAD_REQUEST;
    }
}
