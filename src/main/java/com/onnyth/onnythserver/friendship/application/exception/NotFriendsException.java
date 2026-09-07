package com.onnyth.onnythserver.friendship.application.exception;

import com.onnyth.onnythserver.shared.exception.ApiException;
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
