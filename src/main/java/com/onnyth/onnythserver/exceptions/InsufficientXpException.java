package com.onnyth.onnythserver.exceptions;

import org.springframework.http.HttpStatus;

public class InsufficientXpException extends ApiException {

    public InsufficientXpException(long available, int required) {
        super("Insufficient XP: have " + available + ", need " + required);
    }

    @Override
    public HttpStatus getHttpStatus() {
        return HttpStatus.BAD_REQUEST;
    }
}
