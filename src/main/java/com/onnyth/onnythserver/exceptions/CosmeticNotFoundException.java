package com.onnyth.onnythserver.exceptions;

import org.springframework.http.HttpStatus;

public class CosmeticNotFoundException extends ApiException {

    public CosmeticNotFoundException(String id) {
        super("Cosmetic item not found: " + id);
    }

    @Override
    public HttpStatus getHttpStatus() {
        return HttpStatus.NOT_FOUND;
    }
}
