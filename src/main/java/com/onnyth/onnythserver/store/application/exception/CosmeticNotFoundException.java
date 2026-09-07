package com.onnyth.onnythserver.store.application.exception;

import com.onnyth.onnythserver.shared.exception.ApiException;
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
