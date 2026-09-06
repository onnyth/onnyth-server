package com.onnyth.onnythserver.bookmark.application.command;

import java.util.Set;

public record CreateBookmarkCommand(
        String url,
        String title,
        Set<String> tags
) {
    public static CreateBookmarkCommand of(String url, String title, Set<String> tags) {
        return new CreateBookmarkCommand(url, title, tags);
    }
}

