package com.onnyth.onnythserver.exceptions;

import org.springframework.http.HttpStatus;

public class DuplicateFriendRequestException extends ApiException {

    public DuplicateFriendRequestException(String message) {
        super(message);
    }

    @Override
    public HttpStatus getHttpStatus() {
        return HttpStatus.CONFLICT;
    }
}
