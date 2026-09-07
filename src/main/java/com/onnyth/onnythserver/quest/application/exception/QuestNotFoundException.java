package com.onnyth.onnythserver.quest.application.exception;

import com.onnyth.onnythserver.shared.exception.ApiException;
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
