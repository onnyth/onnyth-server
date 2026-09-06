package com.onnyth.onnythserver.auth.application.exception;

import com.onnyth.onnythserver.shared.exception.ApiException;

import org.springframework.http.HttpStatus;

public class SupabaseUnavailableException extends ApiException {
  public SupabaseUnavailableException() {
    super("Authentication service unavailable");
  }

  @Override
  public HttpStatus getHttpStatus() {
    return HttpStatus.BAD_GATEWAY; // 502
  }
}
