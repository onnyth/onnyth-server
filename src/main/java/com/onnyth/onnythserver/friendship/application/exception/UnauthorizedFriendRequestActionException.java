package com.onnyth.onnythserver.friendship.application.exception;

import com.onnyth.onnythserver.shared.exception.ApiException;
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
