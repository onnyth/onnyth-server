package com.onnyth.onnythserver.shared.exception;

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
