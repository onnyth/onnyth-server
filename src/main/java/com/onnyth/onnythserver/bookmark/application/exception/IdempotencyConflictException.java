package com.onnyth.onnythserver.bookmark.application.exception;

public class IdempotencyConflictException extends RuntimeException {

    public IdempotencyConflictException() {
        super("Idempotency-Key already used with a different request body");
    }
}
