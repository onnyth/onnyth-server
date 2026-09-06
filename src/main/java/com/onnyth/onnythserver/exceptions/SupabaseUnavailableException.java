package com.onnyth.onnythserver.exceptions;

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
