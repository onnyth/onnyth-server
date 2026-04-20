package com.onnyth.onnythserver.exceptions;

import org.springframework.http.HttpStatus;

public class NotFriendsException extends ApiException {

    public NotFriendsException(String message) {
        super(message);
    }

    @Override
    public HttpStatus getHttpStatus() {
        return HttpStatus.BAD_REQUEST;
    }
}
