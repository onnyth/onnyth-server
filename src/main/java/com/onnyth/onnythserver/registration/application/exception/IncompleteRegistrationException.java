package com.onnyth.onnythserver.registration.application.exception;

import com.onnyth.onnythserver.shared.exception.ApiException;

import org.springframework.http.HttpStatus;

import java.util.List;

/**
 * Thrown when POST /registration/complete is called but required steps are missing.
 */
public class IncompleteRegistrationException extends ApiException {

    private final List<String> missingSteps;

    public IncompleteRegistrationException(List<String> missingSteps) {
        super("Required registration steps are not completed: " + String.join(", ", missingSteps));
        this.missingSteps = missingSteps;
    }

    public List<String> getMissingSteps() {
        return missingSteps;
    }

    @Override
    public HttpStatus getHttpStatus() {
        return HttpStatus.BAD_REQUEST;
    }
}
