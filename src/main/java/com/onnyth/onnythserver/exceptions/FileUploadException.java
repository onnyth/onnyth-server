package com.onnyth.onnythserver.exceptions;

import org.springframework.http.HttpStatus;

public class FileUploadException extends ApiException {

    public FileUploadException(String message) {
        super(message);
    }

    @Override
    public HttpStatus getHttpStatus() {
        return HttpStatus.BAD_REQUEST;
    }
}

