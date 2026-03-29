package com.onnyth.onnythserver.exceptions;

import org.springframework.http.HttpStatus;

public class CosmeticAlreadyOwnedException extends ApiException {

    public CosmeticAlreadyOwnedException(String itemId) {
        super("Cosmetic item already owned: " + itemId);
    }

    @Override
    public HttpStatus getHttpStatus() {
        return HttpStatus.CONFLICT;
    }
}
