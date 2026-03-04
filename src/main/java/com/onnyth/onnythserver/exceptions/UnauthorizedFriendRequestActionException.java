package com.onnyth.onnythserver.exceptions;

import org.springframework.http.HttpStatus;

public class UnauthorizedFriendRequestActionException extends ApiException {

    public UnauthorizedFriendRequestActionException(String message) {
        super(message);
    }

    @Override
    public HttpStatus getHttpStatus() {
        return HttpStatus.FORBIDDEN;
    }
}
