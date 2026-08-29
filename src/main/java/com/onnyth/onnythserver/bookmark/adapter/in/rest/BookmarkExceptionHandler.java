package com.onnyth.onnythserver.bookmark.adapter.in.rest;

import com.onnyth.onnythserver.bookmark.application.exception.BookmarkNotFoundException;
import com.onnyth.onnythserver.dto.ApiErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;

@RestControllerAdvice(assignableTypes = BookmarkController.class)
public class BookmarkExceptionHandler {

    @ExceptionHandler(BookmarkNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleBookmarkNotFound(
            BookmarkNotFoundException ex,
            HttpServletRequest request
    ) {
        ApiErrorResponse error = new ApiErrorResponse(
                HttpStatus.NOT_FOUND.value(),
                HttpStatus.NOT_FOUND.getReasonPhrase(),
                ex.getMessage(),
                request.getRequestURI(),
                Instant.now()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }
}

