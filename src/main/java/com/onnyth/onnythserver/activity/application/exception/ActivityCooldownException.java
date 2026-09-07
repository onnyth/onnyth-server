package com.onnyth.onnythserver.activity.application.exception;

import com.onnyth.onnythserver.shared.exception.ApiException;
import org.springframework.http.HttpStatus;

public class ActivityCooldownException extends ApiException {

    public ActivityCooldownException(String message) {
        super(message);
    }

    @Override
    public HttpStatus getHttpStatus() {
        return HttpStatus.TOO_MANY_REQUESTS;
    }
}
