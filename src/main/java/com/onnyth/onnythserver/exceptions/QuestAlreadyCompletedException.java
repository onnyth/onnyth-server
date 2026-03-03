package com.onnyth.onnythserver.exceptions;

import org.springframework.http.HttpStatus;

public class QuestAlreadyCompletedException extends ApiException {

    public QuestAlreadyCompletedException(String questId) {
        super("Quest already completed: " + questId);
    }

    @Override
    public HttpStatus getHttpStatus() {
        return HttpStatus.CONFLICT;
    }
}
