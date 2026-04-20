package com.onnyth.onnythserver.exceptions;

import org.springframework.http.HttpStatus;

public class QuestNotFoundException extends ApiException {

    public QuestNotFoundException(String questId) {
        super("Quest not found: " + questId);
    }

    @Override
    public HttpStatus getHttpStatus() {
        return HttpStatus.NOT_FOUND;
    }
}
