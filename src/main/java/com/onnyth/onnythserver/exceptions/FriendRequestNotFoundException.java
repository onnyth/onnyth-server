package com.onnyth.onnythserver.exceptions;

import com.onnyth.onnythserver.shared.exception.ApiException;

import org.springframework.http.HttpStatus;

public class FriendRequestNotFoundException extends ApiException {

    public FriendRequestNotFoundException(String requestId) {
        super("Friend request not found: " + requestId);
    }

    @Override
    public HttpStatus getHttpStatus() {
        return HttpStatus.NOT_FOUND;
    }
}
