package com.onnyth.onnythserver.exceptions;

import com.onnyth.onnythserver.shared.exception.ApiException;

import org.springframework.http.HttpStatus;

public class InsufficientCoinsException extends ApiException {

    public InsufficientCoinsException(int available, int required) {
        super("Insufficient Onnyth Coins: have " + available + ", need " + required);
    }

    @Override
    public HttpStatus getHttpStatus() {
        return HttpStatus.BAD_REQUEST;
    }
}
