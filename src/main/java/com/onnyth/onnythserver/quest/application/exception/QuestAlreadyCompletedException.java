package com.onnyth.onnythserver.quest.application.exception;

import com.onnyth.onnythserver.shared.exception.ApiException;
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
