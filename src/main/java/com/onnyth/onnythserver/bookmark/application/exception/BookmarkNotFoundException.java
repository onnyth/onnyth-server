package com.onnyth.onnythserver.bookmark.application.exception;

public class BookmarkNotFoundException extends RuntimeException {

    public BookmarkNotFoundException(String bookmarkId) {
        super("Bookmark not found: " + bookmarkId);
    }
}

