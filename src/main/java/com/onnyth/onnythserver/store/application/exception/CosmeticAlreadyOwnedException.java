package com.onnyth.onnythserver.store.application.exception;

import com.onnyth.onnythserver.shared.exception.ApiException;
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
