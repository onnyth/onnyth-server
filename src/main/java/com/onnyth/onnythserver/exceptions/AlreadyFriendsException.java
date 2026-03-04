package com.onnyth.onnythserver.exceptions;

import org.springframework.http.HttpStatus;

public class AlreadyFriendsException extends ApiException {

    public AlreadyFriendsException(String message) {
        super(message);
    }

    @Override
    public HttpStatus getHttpStatus() {
        return HttpStatus.CONFLICT;
    }
}
