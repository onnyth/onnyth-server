package com.onnyth.onnythserver.exceptions;

import org.springframework.http.HttpStatus;

public class QuestExpiredException extends ApiException {

    public QuestExpiredException(String questId) {
        super("Quest has expired: " + questId);
    }

    @Override
    public HttpStatus getHttpStatus() {
        return HttpStatus.BAD_REQUEST;
    }
}
