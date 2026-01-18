package com.onnyth.onnythserver.exceptions.handler;

import com.onnyth.onnythserver.exceptions.ApiException;
import org.springframework.http.HttpStatus;

public class LogoutFailedException extends ApiException {
    public LogoutFailedException(String message) {
        super(message);
    }

  @Override
  public HttpStatus getHttpStatus() {
    return HttpStatus.INTERNAL_SERVER_ERROR;
  }
}
