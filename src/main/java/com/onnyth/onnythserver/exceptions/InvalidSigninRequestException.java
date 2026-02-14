package com.onnyth.onnythserver.exceptions;

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
