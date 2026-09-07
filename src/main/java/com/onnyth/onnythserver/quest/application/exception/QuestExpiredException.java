package com.onnyth.onnythserver.quest.application.exception;

import com.onnyth.onnythserver.shared.exception.ApiException;
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
