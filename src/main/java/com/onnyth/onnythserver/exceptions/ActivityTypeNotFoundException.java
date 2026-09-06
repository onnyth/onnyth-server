package com.onnyth.onnythserver.exceptions;

import com.onnyth.onnythserver.shared.exception.ApiException;

import org.springframework.http.HttpStatus;

public class ActivityTypeNotFoundException extends ApiException {

    public ActivityTypeNotFoundException(String id) {
        super("Activity type not found: " + id);
    }

    @Override
    public HttpStatus getHttpStatus() {
        return HttpStatus.NOT_FOUND;
    }
}
