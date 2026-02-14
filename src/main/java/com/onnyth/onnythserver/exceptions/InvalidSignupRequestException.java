package com.onnyth.onnythserver.exceptions;

import org.springframework.http.HttpStatus;

public class InvalidSignupRequestException extends ApiException {
  public InvalidSignupRequestException(String message) {
    super(message);
  }

  @Override
  public HttpStatus getHttpStatus() {
    return HttpStatus.BAD_REQUEST; // 400
  }
}
